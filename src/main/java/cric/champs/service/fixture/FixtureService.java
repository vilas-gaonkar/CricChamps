package cric.champs.service.fixture;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.entity.*;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.MatchStatus;
import cric.champs.service.TournamentStatus;
import cric.champs.service.TournamentTypes;
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
            throw new FixtureGenerationException("tournament already completed or in progress or team contain less than 2 players");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ?",
                new BeanPropertyRowMapper<>(Grounds.class), tournament.getTournamentId());
        if (grounds.isEmpty())
            throw new NullPointerException("please add ground or umpire");
        List<Umpires> umpires = jdbcTemplate.query("select * from umpires where tournamentId = ?",
                new BeanPropertyRowMapper<>(Umpires.class), tournament.getTournamentId());

        //number of days assigned for this tournament
        int numberOfTournamentDays = Period.between(tournament.getTournamentStartDate(), tournament.getTournamentEndDate()).getDays();

        //number of hours available in one day
        long numberOfHoursPerDayAvailableForPlayingMatch = Duration.between(tournament.getTournamentStartTime().toLocalTime(), tournament.getTournamentEndTime().toLocalTime()).toHours();

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
            generateFixtureForIndividualMatch(tournament);
        else
            throw new FixtureGenerationException("teams required to generate individual match tournament is 2 only");

        throw new FixtureGenerationException("Cannot generate fixture");
    }

    /**
     * Fixture for individual match tournament
     */
    private void generateFixtureForIndividualMatch(Tournaments tournament) {
        Matches matches = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, 1, tournament.getTournamentStartTime().toLocalTime(), tournament.getTournamentEndTime().toLocalTime(),
                tournament.getTournamentStartDate());
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId in(select tournamentId from tournaments where tournamentId = ?)",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());
        insertIntoVersusOfLeague(teams.get(0).getTeamId(), tournament.getTournamentId(), matches.getMatchId());
        insertIntoVersusOfLeague(teams.get(1).getTeamId(), tournament.getTournamentId(), matches.getMatchId());
    }

    /**
     * verifying all condition before Fixture creation for tournament
     */
    private boolean checkAllConditionBeforeFixtureGeneration(Tournaments tournament) {
        if (tournament.getTournamentStatus().equalsIgnoreCase(TournamentStatus.PROGRESS.toString()) ||
                tournament.getTournamentStatus().equalsIgnoreCase(TournamentStatus.COMPLETED.toString()) ||
                tournament.getTournamentStatus().equalsIgnoreCase(TournamentStatus.CANCELLED.toString()))
            return true;
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and numberOfPlayer < 2",
                new BeanPropertyRowMapper<>(Teams.class),tournament.getTournamentId());
        if(teams.size()>0)
            return true;

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
                        tournament.getNumberOfTeams()/2,tournament.getTournamentId());
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
        if (!roundRobinGenerationForKnockoutRoundOne(teamsId, tournament,totalNumberOfMatchExpected))
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
        int matchNumber = 1;
        LocalTime startTime = tournament.getTournamentStartTime().toLocalTime();
        LocalDate startDate = tournament.getTournamentStartDate();
        LocalTime endTime = tournament.getTournamentEndTime().toLocalTime();

        if (teamsId.length % 2 == 1)
            byeTeamId = teamsId[teamsId.length - 1];

        int halfSize = teamsId.length / 2;
        int dummyMatch = totalNumberOfMatchExpected - halfSize;

        LocalTime inningEndTime = getEndTime(tournament, startTime);
        long hour = startTime.until(inningEndTime, ChronoUnit.HOURS);

        for (int teamIdIndex = 0; teamIdIndex < halfSize; teamIdIndex++) {

            if (endTime.isBefore(startTime.plusHours(hour))) {
                startDate = startDate.plusDays(1);
                startTime = tournament.getTournamentStartTime().toLocalTime();
            }
            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, matchNumber, startTime, startTime.plusHours(hour), startDate);
            insertIntoVersusOfLeague(teamsId[teamIdIndex], tournament.getTournamentId(), match.getMatchId());
            insertIntoVersusOfLeague(teamsId[(teamsId.length / 2) + teamIdIndex], tournament.getTournamentId(), match.getMatchId());

            startTime = startTime.plusHours(hour);
            matchNumber++;

        }

        for (int teamIdIndex = 0; teamIdIndex < dummyMatch; teamIdIndex++) {
//            LocalTime inningEndTime = getEndTime(tournament, startTime);
            if (endTime.isBefore(startTime.plusHours(hour))) {
                startDate = startDate.plusDays(1);
                startTime = tournament.getTournamentStartTime().toLocalTime();
            }
            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, matchNumber, startTime, startTime.plusHours(hour), startDate);
            insertIntoVersusOfFinalsForLeague(match.getMatchId());
            insertIntoVersusOfFinalsForLeague(match.getMatchId());

            startTime = startTime.plusHours(hour);
            matchNumber++;
        }
        return true;
    }

    /**
     * Fixture for knockout tournament method one
     */
    private boolean roundRobinGenerationForKnockoutNextMatches(long[] teamsId, Tournaments tournament,int totalNumberOfMatchExpected) {
        long byeTeamId = 0;
        int matchNumber = 1;
        LocalTime startTime = tournament.getTournamentStartTime().toLocalTime();
        LocalDate startDate = tournament.getTournamentStartDate();
        LocalTime endTime = tournament.getTournamentEndTime().toLocalTime();


        if (teamsId.length % 2 == 1) {
            byeTeamId = teamsId[teamsId.length - 1];
        }

        int numberOfPlayingMatches = teamsId.length / 2;
        int numberOfMatchesAfter = totalNumberOfMatchExpected - numberOfPlayingMatches;

        LocalTime inningEndTime = getEndTime(tournament, startTime);
        long hour = startTime.until(inningEndTime, ChronoUnit.HOURS);

        for (int teamIdIndex = 0; teamIdIndex < numberOfPlayingMatches; teamIdIndex++) {
            if (endTime.isBefore(startTime.plusHours(hour))) {
                startDate = startDate.plusDays(1);
                startTime = tournament.getTournamentStartTime().toLocalTime();
            }
            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, matchNumber, startTime, startTime.plusHours(hour), startDate);
            insertIntoVersusOfLeague(teamsId[teamIdIndex], tournament.getTournamentId(), match.getMatchId());
            insertIntoVersusOfLeague(teamsId[teamsId.length - (teamIdIndex + 1)], tournament.getTournamentId(), match.getMatchId());

            startTime = startTime.plusHours(hour);
            matchNumber++;

        }

        for (int teamIdIndex = 0; teamIdIndex < numberOfMatchesAfter; teamIdIndex++) {
            if (endTime.isBefore(startTime.plusHours(hour))) {
                startDate = startDate.plusDays(1);
                startTime = tournament.getTournamentStartTime().toLocalTime();
            }
            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, matchNumber, startTime, startTime.plusHours(hour), startDate);
            insertIntoVersusOfFinalsForLeague(match.getMatchId());
            insertIntoVersusOfFinalsForLeague(match.getMatchId());

            startTime = startTime.plusHours(hour);
            matchNumber++;
        }
        return false;
    }

    /**
     * Fixture for knockout tournament elimination rounds for finals
     */
    private boolean roundRobinGenerationForKnockoutMethodThree(Tournaments tournament,int totalNumberOfMatchExpected) {
        long[] teamsId = getTeamIds(tournament);
        long byeTeamId = 0;
        int halfSize = teamsId.length / 2;
        int numberOfMatchesAfter = totalNumberOfMatchExpected - halfSize;
        int matchNumber = 1;
        LocalTime startTime = tournament.getTournamentStartTime().toLocalTime();
        LocalDate startDate = tournament.getTournamentStartDate();
        LocalTime endTime = tournament.getTournamentEndTime().toLocalTime();

        if (teamsId.length % 2 == 1)
            byeTeamId = teamsId[teamsId.length - 1];

        LocalTime inningEndTime = getEndTime(tournament, startTime);
        long hour = startTime.until(inningEndTime, ChronoUnit.HOURS);

        for (int teamIdIndex = 0; teamIdIndex < teamsId.length; teamIdIndex = teamIdIndex + 2) {
            if (endTime.isBefore(startTime.plusHours(hour))) {
                startDate = startDate.plusDays(1);
                startTime = tournament.getTournamentStartTime().toLocalTime();
            }
            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, matchNumber, startTime, startTime.plusHours(hour), startDate);
            insertIntoVersusOfLeague(teamsId[teamIdIndex], tournament.getTournamentId(), match.getMatchId());
            insertIntoVersusOfLeague(teamsId[teamIdIndex+1], tournament.getTournamentId(), match.getMatchId());

            startTime = startTime.plusHours(hour);
            matchNumber++;
        }

//        for (int teamIdIndex = 0; teamIdIndex < numberOfMatchesAfter; teamIdIndex++) {
//            LocalTime inningEndTime = getEndTime(tournament, startTime);
//            if (endTime.isBefore(startTime.plusHours(inningEndTime.getHour()))) {
//                startDate = startDate.plusDays(1);
//                startTime = tournament.getTournamentStartTime().toLocalTime();
//            }
//            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), 1, matchNumber, startTime, startTime.plusHours(inningEndTime.getHour()), startDate);
//            insertIntoVersusOfLeague(0, tournament.getTournamentId(), match.getMatchId());
//            insertIntoVersusOfLeague(0, tournament.getTournamentId(), match.getMatchId());
//
//            startTime = startTime.plusHours(inningEndTime.getHour());
//            matchNumber++;
//        }
        return false;
    }

    /**
     * Fixture for league tournament
     */
    private SuccessResultModel createRoundRobinForLeague(int totalMatchesCanBePlayedInGivenDatesFormed, Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        if (tournament.getNumberOfTeams() > 2) {
            int totalNumberOfMatchExpected = (tournament.getNumberOfTeams() * (tournament.getNumberOfTeams() - 1) / 2) + 3;
            //checking total matches with expected matches
            if (totalNumberOfMatchExpected > totalMatchesCanBePlayedInGivenDatesFormed)
                throw new FixtureGenerationException("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
            else {
                jdbcTemplate.update("update tournaments set totalRoundRobinMatches = ? where tournamentId = ?",
                        totalNumberOfMatchExpected-3,tournament.getTournamentId());
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
        Matches matches = jdbcTemplate.query("select * from matches order by matchNumber DESC",
                new BeanPropertyRowMapper<>(Matches.class)).get(0);
        int round = matches.getRoundNumber() + 1;
        int matchNumber = matches.getMatchNumber() + 1;
        LocalDate startDate = matches.getMatchDate();
        LocalTime startTime = matches.getMatchEndTime().toLocalTime();
        //final matches
        for (int index = 0; index < numberOfMatches; index++) {
            LocalTime inningEndTime = getEndTime(tournament, startTime);
            if (!inningEndTime.isBefore(tournament.getTournamentEndTime().toLocalTime())) {
                startTime = tournament.getTournamentStartTime().toLocalTime();
                inningEndTime = getEndTime(tournament, startTime);
                startDate = startDate.plusDays(1);
            }
            Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, inningEndTime, startDate);
            insertIntoVersusOfFinalsForLeague(match.getMatchId());
            insertIntoVersusOfFinalsForLeague(match.getMatchId());
            startTime = inningEndTime;
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
        int matchPerGround = matches.size() / tournament.getNumberOfUmpires();
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
        LocalTime inningEndTime;

        if (numberOfTeams % 2 == 0) {
            finalTeamsForRotation = new long[numberOfTeams - 1];
            System.arraycopy(teamsId, 1, finalTeamsForRotation, 0, numberOfTeams - 1);
        } else {
            finalTeamsForRotation = new long[numberOfTeams];
            System.arraycopy(teamsId, 1, finalTeamsForRotation, 0, numberOfTeams - 1);
            finalTeamsForRotation[numberOfTeams - 1] = 0;
        }
        int totalRounds = finalTeamsForRotation.length; //it is even number
        int halfSize = (totalRounds + 1) / 2;
        int round = 0;
        int matchNumber = 1;

        try {
            for (int rounds = totalRounds; rounds > 0; rounds--) {
                System.out.println(++round);
                inningEndTime = getEndTime(tournament, startTime);

                if (!inningEndTime.isBefore(tournament.getTournamentEndTime().toLocalTime()) || tournament.getNumberOfOvers()>40) {
                    startTime = tournament.getTournamentStartTime().toLocalTime();
                    inningEndTime = getEndTime(tournament, startTime);
                    startDate = startDate.plusDays(1);
                }
                Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, inningEndTime, startDate);
                startTime = inningEndTime;
                matchNumber++;
                int teamIdx = rounds % totalRounds;

                //insert into teams
                if (finalTeamsForRotation[teamIdx] != 0) {
                    //insert into verses
                    insertIntoVersusOfLeague(teamsId[0], tournament.getTournamentId(), match.getMatchId());
                    insertIntoVersusOfLeague(finalTeamsForRotation[teamIdx], tournament.getTournamentId(), match.getMatchId());
                }
                for (int i = 1; i < halfSize; i++) {
                    int firstTeam = (rounds + i) % totalRounds;
                    int secondTeam = (rounds + totalRounds - i) % totalRounds;
                    if (finalTeamsForRotation[firstTeam] != 0 && finalTeamsForRotation[secondTeam] != 0) {
                        //insert into versus
                        inningEndTime = getEndTime(tournament, startTime);
                        if (!inningEndTime.isBefore(tournament.getTournamentEndTime().toLocalTime())) {
                            startTime = tournament.getTournamentStartTime().toLocalTime();
                            inningEndTime = getEndTime(tournament, startTime);
                            startDate = startDate.plusDays(1);
                        }
                        Matches nextMatch = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, inningEndTime, startDate);
                        insertIntoVersusOfLeague(finalTeamsForRotation[firstTeam], tournament.getTournamentId(), nextMatch.getMatchId());
                        insertIntoVersusOfLeague(finalTeamsForRotation[secondTeam], tournament.getTournamentId(), nextMatch.getMatchId());
                        startTime = inningEndTime;
                        matchNumber++;
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * add to versus for final
     */
    private void insertIntoVersusOfFinalsForLeague(long matchId) {
        jdbcTemplate.update("insert into versus values (?,?,?,?,?,?,?,?,?)", matchId, null, null, 0, 0, 0, 0, null, "false");
    }

    /**
     * get end time of match
     */
    private LocalTime getEndTime(Tournaments tournament, LocalTime startTime) {
        if (tournament.getNumberOfOvers() < 6)
            return startTime.plusHours(1);
        else if (tournament.getNumberOfOvers() > 6 && tournament.getNumberOfOvers() < 16)
            return startTime.plusHours(2);
        else if (tournament.getNumberOfOvers() > 16 && tournament.getNumberOfOvers() < 31)
            return startTime.plusHours(3);
        else if (tournament.getNumberOfOvers() > 31 && tournament.getNumberOfOvers() < 41)
            return startTime.plusHours(5);
        else
            return startTime.plusHours(8);
    }

    /**
     * add matches for tournament
     */
    private Matches insertIntoMatchesOfLeague(long tournamentId, int round, int matchNumber, LocalTime startTime, LocalTime endTime, LocalDate matchDate) {
        jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, tournamentId, null, null, null, null,
                round, matchNumber, MatchStatus.UPCOMING.toString(), matchDate, DayOfWeek.from(matchDate).name(), startTime, endTime, "false", null);
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
        else if (numberOfOvers > 6 && numberOfOvers < 16)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 2;
        else if (numberOfOvers > 16 && numberOfOvers < 31)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 3;
        else if (numberOfOvers > 31 && numberOfOvers < 41)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 5;
        else
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 8;
    }
}