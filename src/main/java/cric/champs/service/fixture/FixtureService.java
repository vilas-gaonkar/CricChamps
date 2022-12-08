package cric.champs.service.fixture;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.entity.*;
import cric.champs.service.MatchStatus;
import cric.champs.service.TournamentTypes;
import cric.champs.service.system.SystemInterface;
import cric.champs.service.user.TeamInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
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
    public ResultModel generateFixture(long tournamentId) throws Exception {
        int totalNumberOfMatchInOneDay;
        int totalMatchesCanBePlayedInGivenDatesFormed;

        Tournaments tournament = systemInterface.verifyTournamentId(tournamentId).get(0);
        if (tournament == null || tournament.getTournamentType() == null)
            throw new NullPointerException("Invalid tournament id");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ?",
                new BeanPropertyRowMapper<>(Grounds.class), tournament.getTournamentId());
        List<Umpires> umpires = jdbcTemplate.query("select * from umpires where tournamentId = ?",
                new BeanPropertyRowMapper<>(Umpires.class), tournament.getTournamentId());
        if (grounds.isEmpty() || umpires.isEmpty())
            throw new NullPointerException("please add ground or umpire");

        //number of days assigned for this tournament
        int numberOfTournamentDays = Period.between(tournament.getTournamentStartDate(), tournament.getTournamentEndDate()).getDays();

        //number of hours available in one day
        long numberOfHoursPerDayAvailableForPlayingMatch = Duration.between(tournament.getTournamentStartTime().toLocalTime(), tournament.getTournamentEndTime().toLocalTime()).toHours();

        //number of matches can play in one day
        totalNumberOfMatchInOneDay = getNumberMatchPerDay(numberOfHoursPerDayAvailableForPlayingMatch, tournament.getNumberOfOvers());


        //number of matches per day in all grounds according to number of grounds and umpires
        totalMatchesCanBePlayedInGivenDatesFormed = totalNumberOfMatchInOneDay * tournament.getNumberOfGrounds() * numberOfTournamentDays;


        //verifying tournament type to calculate number Of Matches
        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.LEAGUE.toString()))
            return createRoundRobinForLeague(totalMatchesCanBePlayedInGivenDatesFormed, tournament, grounds, umpires);


        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.KNOCKOUT.toString()))
            return createRoundRobinForKnockout(totalMatchesCanBePlayedInGivenDatesFormed, tournament, grounds, umpires);

        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.INDIVIDUALMATCH.toString()) && tournament.getNumberOfTeams() == 2) {
            Matches matches = insertIntoMatchesOfLeague(tournamentId, 1, 1, tournament.getTournamentStartTime().toLocalTime(), tournament.getTournamentEndTime().toLocalTime(),
                    tournament.getTournamentStartDate());
            List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId in(select tournamentId from tournaments where tournamentId = ?)",
                    new BeanPropertyRowMapper<>(Teams.class), tournamentId);
            insertIntoVersusOfLeague(teams.get(0).getTeamId(), tournament.getTournamentId(), matches.getMatchId());
            insertIntoVersusOfLeague(teams.get(1).getTeamId(), tournament.getTournamentId(), matches.getMatchId());
        } else
            throw new FixtureGenerationException("teams required to generate individual match tournament is 2 only");

        throw new FixtureGenerationException("Cannot generate fixture");
    }

    private ResultModel createRoundRobinForKnockout(int totalMatchesCanBePlayedInGivenDatesFormed, Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        if (tournament.getNumberOfTeams() > 1) {
            int totalNumberOfMatchExpected;
            if (tournament.getNumberOfTeams() % 2 == 0)
                totalNumberOfMatchExpected = tournament.getNumberOfTeams() - 1;
            else
                totalNumberOfMatchExpected = tournament.getNumberOfTeams();
            //checking total matches with expected matches
            if (totalNumberOfMatchExpected < totalMatchesCanBePlayedInGivenDatesFormed)
                throw new FixtureGenerationException("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
            else
                return generateFixtureKnockout(tournament, grounds, umpires);
        } else
            throw new FixtureGenerationException("minimum teams required to generate knockout tournament is 2");
    }

    private ResultModel generateFixtureKnockout(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) {
        return null;
    }

    private ResultModel createRoundRobinForLeague(int totalMatchesCanBePlayedInGivenDatesFormed, Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        if (tournament.getNumberOfTeams() > 2) {
            int totalNumberOfMatchExpected = (tournament.getNumberOfTeams() * (tournament.getNumberOfTeams() - 1) / 2) + 3;
            //checking total matches with expected matches
            if (totalNumberOfMatchExpected < totalMatchesCanBePlayedInGivenDatesFormed)
                throw new FixtureGenerationException("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
            else
                return generateFixtureLeague(tournament, grounds, umpires);

        } else
            throw new FixtureGenerationException("minimum teams required to generate league tournament is 3");
    }

    private ResultModel generateFixtureLeague(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted='false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());

        long[] teamsId = new long[teams.size()];
        for (int index = 0; index < teams.size(); index++)
            teamsId[index] = teams.get(index).getTeamId();
        /*int index = 0;
        for (Teams team : teams) {
            teamsId[index] = team.getTeamId();
            index++;
        }*/
        if (!roundRobinGenerationForLeague(teamsId, tournament))
            throw new FixtureGenerationException("cannot generate fixture");
        else {
            assignGroundsAndUmpiresToAllLeagueMatches(grounds, tournament, umpires);
            return new ResultModel("fixture generated successfully");
        }
    }

    private void assignGroundsAndUmpiresToAllLeagueMatches(List<Grounds> grounds, Tournaments tournament, List<Umpires> umpires) {
        List<Matches> matches = jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = 'false' " +
                "order by matchId", new BeanPropertyRowMapper<>(Matches.class), tournament.getTournamentId());
        int matchPerGround = matches.size() / tournament.getNumberOfGrounds();
        int remainingMatches = matches.size() - tournament.getNumberOfGrounds() * matchPerGround;
        assignGroundsToLeague(grounds, matches, matchPerGround, remainingMatches);
        assignUmpiresToAllLeagueMatches(umpires, matches);
    }

    private void assignUmpiresToAllLeagueMatches(List<Umpires> umpires, List<Matches> matches) {

    }

    private void assignGroundsToLeague(List<Grounds> grounds, List<Matches> matches, int matchPerGround, int remainingMatches) {
        for (Grounds ground : grounds)
            for (int matchIndex = 0; matchIndex < matchPerGround; matchIndex++)
                jdbcTemplate.update("update matches set groundId = ? and roundName = ? where matchId = ?",
                        ground.getGroundId(), ground.getGroundName(), matches.get(matchIndex).getMatchId());

        for (Grounds ground : grounds)
            if (remainingMatches != 0) {
                jdbcTemplate.update("update matches set groundId = ? and roundName = ? where matchId = ?",
                        ground.getGroundId(), ground.getGroundName(), matches.get(matches.size() - remainingMatches).getMatchId());
                remainingMatches--;
            } else return;
    }

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
        int round = 1;
        int matchNumber = 1;

        try {
            for (int rounds = totalRounds; rounds > 0; rounds--) {
                //insert into match
                //System.out.println("week " + (round++));
                inningEndTime = getEndTime(tournament, startTime);

                if (!inningEndTime.isBefore(tournament.getTournamentEndTime().toLocalTime())) {
                    startTime = tournament.getTournamentStartTime().toLocalTime();
                    inningEndTime = getEndTime(tournament, startTime);
                    startDate = startDate.plusDays(1);
                }

                Matches match = insertIntoMatchesOfLeague(tournament.getTournamentId(), round, matchNumber, startTime, inningEndTime, startDate);
                startTime = inningEndTime;
                matchNumber++;
                round++;
                /*jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?)", null, tournamentId, null, null,
                        round, matchNumber, MatchStatus.UPCOMING.toString(), null, null, null, null, "false", null);
                Matches match = jdbcTemplate.query("SELECT * FROM matches ORDER BY matchId DESC LIMIT 1",
                        new BeanPropertyRowMapper<>(Matches.class)).get(0);*/
                int teamIdx = rounds % totalRounds;

                //insert into teams
                if (finalTeamsForRotation[teamIdx] != 0) {
                    //insert into verses
                    insertIntoVersusOfLeague(teamsId[0], tournament.getTournamentId(), match.getMatchId());
                    insertIntoVersusOfLeague(teamsId[teamIdx], tournament.getTournamentId(), match.getMatchId());
                /* Teams team = systemInterface.verifyTeamDetails(teamsId[0], tournamentId).get(0);
                jdbcTemplate.update("insert into versus (?,?,?,?,?,?,?,?,?)", match.getMatchId(), teamsId[0],
                        team.getTeamName(), 0, 0, 0, 0, null, "false");

                System.out.println(teamsId[0] + " vs. " + finalTeamsForRotation[teamIdx]);*/
                }
                for (int i = 1; i < halfSize; i++) {

                    int firstTeam = (rounds + i) % totalRounds;
                    int secondTeam = (rounds + totalRounds - i) % totalRounds;
                    if (finalTeamsForRotation[firstTeam] != 0 && finalTeamsForRotation[secondTeam] != 0) {
                        //System.out.println(finalTeamsForRotation[firstTeam] + " vs. " + finalTeamsForRotation[secondTeam]);
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
            //final matches
            for (int index = 0; index < 3; index++) {
                inningEndTime = getEndTime(tournament, startTime);
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
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private void insertIntoVersusOfFinalsForLeague(long matchId) {
        jdbcTemplate.update("insert into versus (?,?,?,?,?,?,?,?,?)", matchId, null, null, 0, 0, 0, 0, null, "false");
    }

    private LocalTime getEndTime(Tournaments tournament, LocalTime startTime) {
        if (tournament.getNumberOfOvers() < 6)
            return startTime.plusHours(1);
        else if (tournament.getNumberOfOvers() > 6 && tournament.getNumberOfOvers() < 16)
            return startTime.plusHours(2);
        else if (tournament.getNumberOfOvers() > 16 && tournament.getNumberOfOvers() < 31)
            return startTime.plusHours(3);
        else
            return startTime.plusHours(5);
    }

    private Matches insertIntoMatchesOfLeague(long tournamentId, int round, int matchNumber, LocalTime startTime, LocalTime endTime, LocalDate matchDate) {
        jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?)", null, tournamentId, null, null,
                round, matchNumber, MatchStatus.UPCOMING.toString(), matchDate, DayOfWeek.from(matchDate).name(), startTime, endTime, "false", null);
        return jdbcTemplate.query("SELECT * FROM matches ORDER BY matchId DESC LIMIT 1",
                new BeanPropertyRowMapper<>(Matches.class)).get(0);
    }

    private void insertIntoVersusOfLeague(long teamId, long tournamentId, long matchId) {
        jdbcTemplate.update("insert into versus (?,?,?,?,?,?,?,?,?)", matchId, teamId, systemInterface.
                verifyTeamDetails(teamId, tournamentId).get(0).getTeamName(), 0, 0, 0, 0, null, "false");
    }

    private int getNumberMatchPerDay(long numberOfHoursPerDayAvailableForPlayingMatch, int numberOfOvers) {
        if (numberOfOvers < 6)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch;
        else if (numberOfOvers > 6 && numberOfOvers < 16)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 2;
        else if (numberOfOvers > 16 && numberOfOvers < 31)
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 3;
        else
            return (int) numberOfHoursPerDayAvailableForPlayingMatch / 5;
    }
}
