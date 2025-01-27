package cric.champs.service.fixture;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.entity.*;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.*;
import cric.champs.service.system.SystemInterface;
import cric.champs.service.user.TeamInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FixtureService implements FixtureGenerationInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Autowired
    private TeamInterface teamInterface;

    @Override
    public SuccessResultModel generateFixture(long tournamentId) throws Exception {
        int totalNumberOfMatchInOneDay;
        int totalMatchesCanBePlayedInGivenDatesFormed;
        Tournaments tournament = systemInterface.verifyTournamentId(tournamentId).get(0);
        if (tournament == null || tournament.getTournamentType() == null)
            throw new NullPointerException("Invalid tournament id");
        if (checkAllConditionBeforeFixtureGeneration(tournament))
            throw new FixtureGenerationException("tournament already completed or in progress or one of the team contain less than 2 players");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Grounds.class), tournament.getTournamentId());
        if (grounds.isEmpty())
            throw new NullPointerException("please add ground or umpire");
        List<Umpires> umpires = jdbcTemplate.query("select * from umpires where tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Umpires.class), tournament.getTournamentId());

        //number of days assigned for this tournament
        int numberOfTournamentDays = Period.between(tournament.getTournamentStartDate(), tournament.getTournamentEndDate()).getDays();

        //number of hours available in one day
        long numberOfHoursPerDayAvailableForPlayingMatch = Duration.between(tournament.getTournamentStartTime().toLocalTime(),
                tournament.getTournamentEndTime().toLocalTime()).toHours();

        //number of matches can play in one day
        totalNumberOfMatchInOneDay = getNumberMatchPerDay(numberOfHoursPerDayAvailableForPlayingMatch, tournament.getNumberOfOvers());

        //number of matches per day in all grounds according to number of grounds and umpires
        totalMatchesCanBePlayedInGivenDatesFormed = totalNumberOfMatchInOneDay * tournament.getNumberOfGrounds() * numberOfTournamentDays;

        //Fixture for league tournament
        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.LEAGUE.toString()))
            return createRoundRobinForLeague(totalMatchesCanBePlayedInGivenDatesFormed, tournament, grounds, umpires);

        //Fixture for knockout tournament
        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.KNOCKOUT.toString()))
            return createRoundRobinForKnockout(totalMatchesCanBePlayedInGivenDatesFormed, tournament, grounds, umpires);

        //Fixture for individual match tournament
        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.INDIVIDUALMATCH.toString()) && tournament.getNumberOfTeams() == 2)
            return generateFixtureForIndividualMatch(tournament, grounds, umpires);
        throw new FixtureGenerationException("Cannot generate fixture");
    }

    /**
     * Fixture for individual match tournament
     */
    private SuccessResultModel generateFixtureForIndividualMatch(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) {
        Matches matches = insertIntoMatchesOfIndividual(tournament.getTournamentId(),
                tournament.getTournamentStartTime().toLocalTime(), tournament.getTournamentEndTime().toLocalTime(),
                tournament.getTournamentStartDate(), grounds.get(0), umpires.isEmpty() ? null : umpires.get(0));
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId in(select tournamentId from" +
                        " tournaments where tournamentId = ?) and isDeleted = 'false' ",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());
        insertIntoVersusOfLeague(teams.get(0).getTeamId(), tournament.getTournamentId(), matches.getMatchId());
        insertIntoVersusOfLeague(teams.get(1).getTeamId(), tournament.getTournamentId(), matches.getMatchId());
        return new SuccessResultModel("fixture generated successfully");
    }

    private Matches insertIntoMatchesOfIndividual(long tournamentId, LocalTime startTime, LocalTime endTime, LocalDate matchDate, Grounds ground, Umpires umpires) {
        Long umpireId = null;
        String umpireName = null;
        if (umpires != null) {
            umpireId = umpires.getUmpireId();
            umpireName = umpires.getUmpireName();
        }
        jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, tournamentId,
                ground.getGroundId(), ground.getGroundName(), umpireId, umpireName, 1, 1, MatchStatus.UPCOMING.toString(), matchDate,
                DayOfWeek.from(matchDate).name(), startTime, endTime, 0, "false", null);
        return jdbcTemplate.query("SELECT * FROM matches ORDER BY matchId DESC LIMIT 1",
                new BeanPropertyRowMapper<>(Matches.class)).get(0);
    }

    /**
     * verifying all condition before Fixture creation for tournament
     */
    private boolean checkAllConditionBeforeFixtureGeneration(Tournaments tournament) {
        if (tournament.getTournamentStatus().equalsIgnoreCase(TournamentStatus.PROGRESS.toString()) ||
                tournament.getTournamentStatus().equalsIgnoreCase(TournamentStatus.COMPLETED.toString()) ||
                tournament.getTournamentStatus().equalsIgnoreCase(TournamentStatus.CANCELLED.toString()))
            return true;
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and numberOfPlayers < 2 " +
                "and isDeleted = 'false'", new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());
        if (teams.size() > 0)
            return true;
        List<Matches> matches = jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = " +
                "'false' order by matchNumber DESC", new BeanPropertyRowMapper<>(Matches.class), tournament.getTournamentId());
        if (!matches.isEmpty()) {
            jdbcTemplate.update("delete from matches where tournamentId = ? and isCancelled = 'false'",
                    tournament.getTournamentId());
            return false;
        }
        return false;
    }

    /**
     * Fixture for knockout tournament
     */
    private SuccessResultModel createRoundRobinForKnockout(int totalMatchesCanBePlayedInGivenDatesFormed, Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        if (tournament.getNumberOfTeams() > 1) {
            int totalNumberOfMatchExpected = tournament.getNumberOfTeams() - 1;
            //checking total matches with expected matches
            if (totalNumberOfMatchExpected > totalMatchesCanBePlayedInGivenDatesFormed)
                throw new FixtureGenerationException("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
            else {
                jdbcTemplate.update("update tournaments set totalRoundRobinMatches = ? where tournamentId = ?",
                        tournament.getNumberOfTeams() / 2, tournament.getTournamentId());
                return generateFixtureKnockout(tournament, grounds, umpires, totalNumberOfMatchExpected);
            }
        } else
            throw new FixtureGenerationException("minimum teams required to generate knockout tournament is 2");
    }

    /**
     * Fixture for knockout tournament
     */
    private SuccessResultModel generateFixtureKnockout(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires, int totalNumberOfMatchExpected) throws FixtureGenerationException {
        long[] teamsId = getTeamIds(tournament);
        if (!roundRobinGenerationForKnockoutRoundOne(teamsId, tournament, totalNumberOfMatchExpected))
            throw new FixtureGenerationException("cannot generate fixture");
        else {
            assignGroundsAndUmpiresToAllLeagueMatches(grounds, tournament, umpires);
            return new SuccessResultModel("fixture generated successfully");
        }
    }

    /**
     * Fixture for knockout tournament round one
     */
    private boolean roundRobinGenerationForKnockoutRoundOne(long[] teamsId, Tournaments tournament, int totalNumberOfMatchExpected) {
        long byeTeamId = 0;
        boolean isBye = false;
        int matchNumber = 1;
        int round = 1;
        LocalTime startTime = tournament.getTournamentStartTime().toLocalTime();
        LocalDate startDate = tournament.getTournamentStartDate();
        LocalTime endTime = tournament.getTournamentEndTime().toLocalTime();
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(startDate, endTime);

        if (teamsId.length % 2 == 1) {
            byeTeamId = teamsId[teamsId.length - 1];
            isBye = true;
        }
        int halfSize = teamsId.length / 2;
        int dummyMatch = totalNumberOfMatchExpected - halfSize;
        try {
            LocalTime inningEndTime = getEndTime(tournament, startTime);
            long hour = startTime.until(inningEndTime, ChronoUnit.HOURS);
            for (int teamIdIndex = 0; teamIdIndex < halfSize; teamIdIndex++) {
                if (endDateTime.isBefore(startDateTime.plusHours(hour))) {
                    startDate = startDate.plusDays(1);
                    startDateTime = LocalDateTime.of(startDate, tournament.getTournamentStartTime().toLocalTime());
                    endDateTime = endDateTime.plusDays(1);
                }
                Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startDateTime.toLocalTime(), startDateTime.plusHours(hour).toLocalTime(), startDate);
                insertIntoVersusOfLeague(teamsId[teamIdIndex], tournament.getTournamentId(), match.getMatchId());
                insertIntoVersusOfLeague(teamsId[(teamsId.length / 2) + teamIdIndex], tournament.getTournamentId(), match.getMatchId());
                startDateTime = startDateTime.plusHours(hour);
                matchNumber++;
            }
            if (isBye) {
                jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null,
                        tournament.getTournamentId(), null, null, null, null, round, matchNumber,
                        MatchStatus.BYE.toString(), LocalDate.now(), DayOfWeek.from(LocalDate.now()).name(),
                        LocalTime.now(), LocalTime.now(), 0, "false", null);
                Matches matches = jdbcTemplate.query("SELECT * FROM matches ORDER BY matchId DESC LIMIT 1",
                        new BeanPropertyRowMapper<>(Matches.class)).get(0);
                jdbcTemplate.update("insert into versus values(?,?,?,?,?,?,?,?,?)", matches.getMatchId(),
                        byeTeamId, systemInterface.verifyTeamDetails(byeTeamId, tournament.getTournamentId()).get(0).getTeamName(),
                        0, 0, 0, 0, VersusStatus.WIN.toString(), "false");
                jdbcTemplate.update("update teams set teamStatus = ? where teamId = ?  and tournamentId = ?",
                        TeamStatus.WIN.toString(), byeTeamId, tournament.getTournamentId());
                matchNumber++;
            }
            for (int teamIdIndex = 0; teamIdIndex < dummyMatch; teamIdIndex++) {
                if (endDateTime.isBefore(startDateTime.plusHours(hour))) {
                    startDate = startDate.plusDays(1);
                    startDateTime = LocalDateTime.of(startDate, tournament.getTournamentStartTime().toLocalTime());
                    endDateTime = endDateTime.plusDays(1);
                }
                insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startDateTime.toLocalTime(),
                        startDateTime.toLocalTime().plusHours(hour), startDate);
                startDateTime = startDateTime.plusHours(hour);
                matchNumber++;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fixture for finals knockout
     */
    @Override
    public void roundRobinGenerationForKnockoutNextMatches(Tournaments tournament) {
        long byeTeamId = 0;
        boolean isBye = false;
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and teamStatus = ? " +
                        "and isDeleted = 'false' order by teamId DESC",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId(), TeamStatus.WIN.toString());
        jdbcTemplate.update("update tournaments set totalRoundRobinMatches = ?, totalMatchesCompleted = ? where tournamentId = ?",
                teams.size() / 2, 0, tournament.getTournamentId());
        long[] teamsId = new long[teams.size()];
        for (int index = 0; index < teams.size(); index++)
            teamsId[index] = teams.get(index).getTeamId();
        if (teamsId.length % 2 == 1) {
            byeTeamId = teamsId[teamsId.length - 1];
            isBye = true;
        }
        List<Matches> matches = jdbcTemplate.query("select * from matches where tournamentId = ? and matchStatus = ?",
                new BeanPropertyRowMapper<>(Matches.class), tournament.getTournamentId(), MatchStatus.UPCOMING.toString());
        List<Matches> matches1 = jdbcTemplate.query("select * from matches where tournamentId = ? and matchStatus = ? order by matchId DESC limit 1",
                new BeanPropertyRowMapper<>(Matches.class), tournament.getTournamentId(), MatchStatus.PAST.toString());
        long matchId = matches.get(0).getMatchId();
        int round = matches1.get(0).getRoundNumber() + 1;
        for (int teamIdIndex = 0; teamIdIndex < teamsId.length / 2; teamIdIndex = teamIdIndex + 2) {
            insertIntoVersusOfLeague(teamsId[teamIdIndex], tournament.getTournamentId(), matchId);
            insertIntoVersusOfLeague(teamsId[teamIdIndex + 1], tournament.getTournamentId(), matchId);
            jdbcTemplate.update("update matches set roundNumber = ? where matchId = ?", round, matchId);
            matchId++;
        }
        if (isBye)
            jdbcTemplate.update("update teams set teamStatus = ? where teamId = ?  and tournamentId = ?",
                    TeamStatus.WIN.toString(), byeTeamId, tournament.getTournamentId());
    }

    /**
     * Fixture for finals league ***use***
     */
    @Override
    public void roundRobinGenerationForKnockoutLeague(long tournamentId, String tournamentStage) {
        if (tournamentStage.equals(TournamentStage.SEMIFINALS.toString())) {
            long[] teamsId = getTeamIdForLeagueSemiFinals(tournamentId);
            List<Matches> matches = jdbcTemplate.query("select * from matches where tournamentId = ? order by " +
                    "matchNumber DESC limit 3", new BeanPropertyRowMapper<>(Matches.class), tournamentId);
            jdbcTemplate.update("update tournaments set totalRoundRobinMatches = 2 , totalMatchesCompleted  = 0" +
                    " where tournamentId = ?", tournamentId);
            int matchSize = matches.size() - 1;
            try {
                for (int teamIdIndex = 0; teamIdIndex < teamsId.length; teamIdIndex = teamIdIndex + 2) {
                    addTOVersus(teamsId[teamIdIndex], tournamentId, matches.get(matchSize).getMatchId());
                    addTOVersus(teamsId[teamIdIndex + 1], tournamentId, matches.get(matchSize).getMatchId());
                    matchSize--;
                }
            } catch (Exception e) {
                addTOVersus(teamsId[teamsId.length - 2], tournamentId, matches.get(matchSize).getMatchId());
            }
        } else if (tournamentStage.equals(TournamentStage.FINALS.toString())) {
            jdbcTemplate.update("update tournaments set totalRoundRobinMatches = 1 , totalMatchesCompleted  = 0" +
                    " where tournamentId = ?", tournamentId);
            List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted =" +
                    " 'false' order by points DESC limit 2", new BeanPropertyRowMapper<>(Teams.class), tournamentId);
            Matches match = jdbcTemplate.query("select * from matches where tournamentId = ? order by " +
                    "matchNumber DESC limit 1", new BeanPropertyRowMapper<>(Matches.class), tournamentId).get(0);
            addTOVersus(teams.get(0).getTeamId(), tournamentId, match.getMatchId());
            addTOVersus(teams.get(1).getTeamId(), tournamentId, match.getMatchId());
        }
    }

    private void addTOVersus(long teamId, long tournamentId, long matchId) {
        jdbcTemplate.update("insert into versus values(?,?,?,?,?,?,?,?,?)", matchId, teamId,
                systemInterface.verifyTeamDetails(teamId, tournamentId).get(0).getTeamName(), 0, 0, 0, 0, null, "false");
    }

    private long[] getTeamIdForLeagueFinals(long tournamentId) {
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted = 'false' " +
                "order by points DESC limit 2", new BeanPropertyRowMapper<>(Teams.class), tournamentId);
        long[] teamsId = new long[teams.size()];
        for (int index = 0; index < teams.size(); index++)
            teamsId[index] = teams.get(index).getTeamId();
        return teamsId;
    }

    public long[] getTeamIdForLeagueSemiFinals(long tournamentId) {
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted = 'false' " +
                "order by points DESC limit 4", new BeanPropertyRowMapper<>(Teams.class), tournamentId);
        long[] teamsId = new long[teams.size()];
        for (int index = 0; index < teams.size(); index++)
            teamsId[index] = teams.get(index).getTeamId();
        return teamsId;
    }

    /**
     * Fixture for league tournament
     */
    private SuccessResultModel createRoundRobinForLeague(int totalMatchesCanBePlayedInGivenDatesFormed, Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        if (tournament.getNumberOfTeams() == 2) {
            jdbcTemplate.update("update tournaments set totalRoundRobinMatches = 1 where tournamentId = ?",
                    tournament.getTournamentId());
            generateFixtureForIndividualMatch(tournament, grounds, umpires);
        }
        if (tournament.getNumberOfTeams() > 2) {
            int totalNumberOfMatchExpected = (tournament.getNumberOfTeams() * (tournament.getNumberOfTeams() - 1) / 2) + 3;
            //checking total matches with expected matches
            if (totalNumberOfMatchExpected > totalMatchesCanBePlayedInGivenDatesFormed)
                throw new FixtureGenerationException("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
            else {
                jdbcTemplate.update("update tournaments set totalRoundRobinMatches = ? where tournamentId = ?",
                        totalNumberOfMatchExpected - 3, tournament.getTournamentId());
                return generateFixtureLeague(tournament, grounds, umpires);
            }
        } else
            throw new FixtureGenerationException("minimum teams required to generate league tournament is 3");
    }

    /**
     * Fixture for league tournament
     */
    private SuccessResultModel generateFixtureLeague(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        long[] teamsId = getTeamIds(tournament);
        if (!roundRobinGenerationForLeague(teamsId, tournament))
            throw new FixtureGenerationException("cannot generate fixture");
        else {
            addEliminationMatchesToTournament(tournament, 2);
            addEliminationMatchesToTournament(tournament, 1);
            assignGroundsAndUmpiresToAllLeagueMatches(grounds, tournament, umpires);
            return new SuccessResultModel("fixture generated successfully");
        }
    }

    /**
     * assigning final matches
     */
    private void addEliminationMatchesToTournament(Tournaments tournament, int numberOfMatches) {
        Matches matches = jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = 'false'" +
                        " order by matchNumber DESC limit 1", new BeanPropertyRowMapper<>(Matches.class),
                tournament.getTournamentId()).get(0);
        int round = matches.getRoundNumber() + 1;
        int matchNumber = matches.getMatchNumber() + 1;
        LocalDate startDate = matches.getMatchDate();
        LocalTime startTime = matches.getMatchEndTime().toLocalTime();
        //final matches
        for (int index = 0; index < numberOfMatches; index++) {
            LocalDateTime matchEndTimes = getEndDateTime(tournament, LocalDateTime.of(startDate, startTime));
            LocalTime matchEndTime = matchEndTimes.toLocalTime();
            if (!matchEndTimes.isBefore(LocalDateTime.of(startDate, tournament.getTournamentEndTime().toLocalTime()))) {
                startTime = tournament.getTournamentStartTime().toLocalTime();
                matchEndTime = getEndDateTime(tournament, LocalDateTime.of(startDate, startTime)).toLocalTime();
                startDate = startDate.plusDays(1);
            }
            insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, matchEndTime, startDate);
            startTime = matchEndTime;
            matchNumber++;
        }
    }

    /**
     * get array of all team id's of tournament
     */
    private long[] getTeamIds(Tournaments tournament) {
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted='false'",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());
        long[] teamsId = new long[teams.size()];
        for (int index = 0; index < teams.size(); index++)
            teamsId[index] = teams.get(index).getTeamId();
        return teamsId;
    }

    /**
     * assign grounds and umpires for all tournament matches
     */
    private void assignGroundsAndUmpiresToAllLeagueMatches(List<Grounds> grounds, Tournaments tournament, List<Umpires> umpires) {
        List<Matches> matches = jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = 'false' " +
                "order by matchId", new BeanPropertyRowMapper<>(Matches.class), tournament.getTournamentId());
        int matchPerGround = matches.size() / tournament.getNumberOfGrounds();
        int remainingMatches = matches.size() - tournament.getNumberOfGrounds() * matchPerGround;
        assignGroundsToLeague(grounds, matches, matchPerGround, remainingMatches);
        assignUmpiresToAllLeagueMatches(umpires, matches, tournament);
    }

    /**
     * assign umpires for all tournament matches
     */
    private void assignUmpiresToAllLeagueMatches(List<Umpires> umpires, List<Matches> matches, Tournaments tournament) {
        int matchPerGround = tournament.getNumberOfUmpires() == 0 ? 0 : matches.size() / tournament.getNumberOfUmpires();
        int remainingMatches = matches.size() - tournament.getNumberOfUmpires() * matchPerGround;
        int matchCount = 0;
        for (Umpires umpire : umpires)
            for (int matchIndex = 0; matchIndex < matchPerGround; matchIndex++)
                jdbcTemplate.update("update matches set umpireId = ?, umpireName = ? where matchId = ?",
                        umpire.getUmpireId(), umpire.getUmpireName(), matches.get(matchCount++).getMatchId());
        for (Umpires umpire : umpires)
            if (remainingMatches != 0)
                jdbcTemplate.update("update matches set umpireId = ?, umpireName = ? where matchId = ?",
                        umpire.getUmpireId(), umpire.getUmpireName(), matches.get(matches.size() - remainingMatches--).getMatchId());
    }

    /**
     * assign grounds for all tournament matches
     */
    private void assignGroundsToLeague(List<Grounds> grounds, List<Matches> matches, int matchPerGround, int remainingMatches) {
        int matchCount = 0;
        for (Grounds ground : grounds)
            for (int matchIndex = 0; matchIndex < matchPerGround; matchIndex++)
                jdbcTemplate.update("update matches set groundId = ?,groundName = ? where matchId = ?",
                        ground.getGroundId(), ground.getGroundName(), matches.get(matchCount++).getMatchId());
        for (Grounds ground : grounds)
            if (remainingMatches != 0)
                jdbcTemplate.update("update matches set groundId = ?, groundName = ? where matchId = ?",
                        ground.getGroundId(), ground.getGroundName(), matches.get(matches.size() - remainingMatches--).getMatchId());
    }

    /**
     * Fixture for league tournament
     */
    private boolean roundRobinGenerationForLeague(long[] teamsId, Tournaments tournament) {
        int numberOfTeams = teamsId.length;
        long[] finalTeamsForRotation;
        LocalTime startTime = tournament.getTournamentStartTime().toLocalTime();
        LocalDate startDate = tournament.getTournamentStartDate();
        LocalTime matchEndTime;

        if (numberOfTeams % 2 == 0) {
            finalTeamsForRotation = new long[numberOfTeams - 1];
            System.arraycopy(teamsId, 1, finalTeamsForRotation, 0, numberOfTeams - 1);
        } else {
            finalTeamsForRotation = new long[numberOfTeams];
            System.arraycopy(teamsId, 1, finalTeamsForRotation, 0, numberOfTeams - 1);
            finalTeamsForRotation[numberOfTeams - 1] = 0;
        }
        int totalRounds = finalTeamsForRotation.length;
        int halfSize = (totalRounds + 1) / 2;
        int round = 0;
        int matchNumber = 1;

        try {
            for (int rounds = totalRounds; rounds > 0; rounds--) {
                ++round;
                LocalDateTime matchEndTimes = getEndDateTime(tournament, LocalDateTime.of(startDate, startTime));
                matchEndTime = matchEndTimes.toLocalTime();
                if (!matchEndTimes.isBefore(LocalDateTime.of(startDate, tournament.getTournamentEndTime().toLocalTime()))) {
                    startTime = tournament.getTournamentStartTime().toLocalTime();
                    matchEndTime = getEndDateTime(tournament, LocalDateTime.of(startDate, startTime)).toLocalTime();
                    startDate = startDate.plusDays(1);
                }
                int teamIdx = rounds % totalRounds;
                if (finalTeamsForRotation[teamIdx] != 0) {
                    Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, matchEndTime, startDate);
                    startTime = matchEndTime;
                    matchNumber++;
                    insertIntoVersusOfLeague(teamsId[0], tournament.getTournamentId(), match.getMatchId());
                    insertIntoVersusOfLeague(finalTeamsForRotation[teamIdx], tournament.getTournamentId(), match.getMatchId());
                }
                for (int i = 1; i < halfSize; i++) {
                    int firstTeam = (rounds + i) % totalRounds;
                    int secondTeam = (rounds + totalRounds - i) % totalRounds;
                    if (finalTeamsForRotation[firstTeam] != 0 && finalTeamsForRotation[secondTeam] != 0) {
                        matchEndTimes = getEndDateTime(tournament, LocalDateTime.of(startDate, startTime));
                        matchEndTime = matchEndTimes.toLocalTime();
                        if (!matchEndTimes.isBefore(LocalDateTime.of(startDate, tournament.getTournamentEndTime().toLocalTime()))) {
                            startTime = tournament.getTournamentStartTime().toLocalTime();
                            matchEndTime = getEndDateTime(tournament, LocalDateTime.of(startDate, startTime)).toLocalTime();
                            startDate = startDate.plusDays(1);
                        }
                        Matches nextMatch = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, matchEndTime, startDate);
                        insertIntoVersusOfLeague(finalTeamsForRotation[firstTeam], tournament.getTournamentId(), nextMatch.getMatchId());
                        insertIntoVersusOfLeague(finalTeamsForRotation[secondTeam], tournament.getTournamentId(), nextMatch.getMatchId());
                        startTime = matchEndTime;
                        matchNumber++;
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private LocalDateTime getEndDateTime(Tournaments tournament, LocalDateTime startDateTime) {
        if (tournament.getNumberOfOvers() < 6)
            return startDateTime.plusHours(1);
        else if (tournament.getNumberOfOvers() < 16)
            return startDateTime.plusHours(2);
        else if (tournament.getNumberOfOvers() < 31)
            return startDateTime.plusHours(3);
        else if (tournament.getNumberOfOvers() < 41)
            return startDateTime.plusHours(5);
        else
            return startDateTime.plusHours(8);
    }

    /**
     * get end time of match
     */
    private LocalTime getEndTime(Tournaments tournament, LocalTime startTime) {
        if (tournament.getNumberOfOvers() < 6)
            return startTime.plusHours(1);
        else if (tournament.getNumberOfOvers() < 16)
            return startTime.plusHours(2);
        else if (tournament.getNumberOfOvers() < 31)
            return startTime.plusHours(3);
        else if (tournament.getNumberOfOvers() < 41)
            return startTime.plusHours(5);
        else
            return startTime.plusHours(8);
    }

    /**
     * add matches for tournament
     */
    private Matches insertIntoMatchesOfLeague(long tournamentId, int round, int matchNumber, LocalTime startTime, LocalTime endTime, LocalDate matchDate) {
        jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, tournamentId,
                null, null, null, null, round, matchNumber, MatchStatus.UPCOMING.toString(), matchDate,
                DayOfWeek.from(matchDate).name(), startTime, endTime, 0, "false", null);
        return jdbcTemplate.query("SELECT * FROM matches ORDER BY matchId DESC LIMIT 1",
                new BeanPropertyRowMapper<>(Matches.class)).get(0);
    }

    /**
     * add to versus for matches of tournament
     */
    private void insertIntoVersusOfLeague(long teamId, long tournamentId, long matchId) {
        jdbcTemplate.update("insert into versus values(?,?,?,?,?,?,?,?,?)", matchId, teamId, systemInterface.
                verifyTeamDetails(teamId, tournamentId).get(0).getTeamName(), 0, 0, 0, 0, null, "false");
    }

    /**
     * get number of match per day
     */
    private int getNumberMatchPerDay(long numberOfHoursPerDayAvailableForPlayingMatch, int numberOfOvers) {
        if (numberOfOvers < 6)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch;
        else if (numberOfOvers < 16)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 2;
        else if (numberOfOvers < 31)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 3;
        else if (numberOfOvers < 41)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 5;
        else
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 8;
    }
}