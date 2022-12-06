package cric.champs.service.fixture;

import cric.champs.entity.*;
import cric.champs.service.MatchStatus;
import cric.champs.service.TournamentTypes;
import cric.champs.service.system.SystemInterface;
import cric.champs.service.user.TeamInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Period;
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
        int totalNumberOfMatchExpected;
        int totalNumberOfMatchInOneDay;
        int totalMatchesCanBePlayedInGivenDatesFormed;

        Tournaments tournament = systemInterface.verifyTournamentId(tournamentId).get(0);
        if (tournament == null || tournament.getTournamentType() == null)
            return new ResultModel("Invalid tournament id");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ?",
                new BeanPropertyRowMapper<>(Grounds.class), tournament.getTournamentId());
        List<Umpires> umpires = jdbcTemplate.query("select * from umpires where tournamentId = ?",
                new BeanPropertyRowMapper<>(Umpires.class), tournament.getTournamentId());
        if (grounds.isEmpty() || umpires.isEmpty())
            return new ResultModel("please add ground or umpire");

        //number of days assigned for this tournament
        int numberOfTournamentDays = Period.between(tournament.getTournamentStartDate(), tournament.getTournamentEndDate()).getDays();

        //number of hours available in one day
        long numberOfHoursPerDayAvailableForPlayingMatch = Duration.between(tournament.getTournamentStartTime().toLocalTime(), tournament.getTournamentEndTime().toLocalTime()).toHours();

        //number of matches can play in one day
        totalNumberOfMatchInOneDay = getNumberMatchPerDay(numberOfHoursPerDayAvailableForPlayingMatch, tournament.getNumberOfOvers());


        //number of matches per day in all grounds according to number of grounds and umpires
        totalMatchesCanBePlayedInGivenDatesFormed = totalNumberOfMatchInOneDay * tournament.getNumberOfGrounds() * numberOfTournamentDays;

        //verifying tournament type to calculate number Of Matches
        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.LEAGUE.toString()) && tournament.getNumberOfTeams() > 2)
            totalNumberOfMatchExpected = (tournament.getNumberOfTeams() * (tournament.getNumberOfTeams() - 1) / 2) + 3;
        else
            throw new NullPointerException("minimum teams required to generate league tournament is 3");

        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.KNOCKOUT.toString()) && tournament.getNumberOfTeams() > 1)
            totalNumberOfMatchExpected = tournament.getNumberOfTeams() - 1;
        else
            throw new NullPointerException("minimum teams required to generate knockout tournament is 2");

        if (tournament.getTournamentType().equalsIgnoreCase(TournamentTypes.INDIVIDUALMATCH.toString()) && tournament.getNumberOfTeams() == 2)
            totalNumberOfMatchExpected = 1;
        else
            throw new NullPointerException("teams required to generate individual match tournament is 2 only");

        //checking total matches with expected matches
        if (totalNumberOfMatchExpected < totalMatchesCanBePlayedInGivenDatesFormed)
            throw new Exception("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
        else
            return generateFixture(tournament, grounds, umpires);

    }

    private ResultModel generateFixture(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) throws Exception {
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted='false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());

        long[] teamsId = new long[teams.size()];
        for(int index = 0; index <teams.size(); index++)
            teamsId[index] = teams.get(index).getTeamId();
        /*int index = 0;
        for (Teams team : teams) {
            teamsId[index] = team.getTeamId();
            index++;
        }*/
        if(!roundRobinGeneration(teamsId, tournament.getTournamentId()))
            throw new Exception("cannot generate fixture");
        else {
            assignGroundsToAllMatches(grounds);
            assignTimeToAllMatches(tournament);
            assignUmpiresToAllMatches(umpires);
            return new ResultModel("fixture generated successfully");
        }
    }

    private void assignUmpiresToAllMatches(List<Umpires> umpires) {
    }

    private void assignGroundsToAllMatches(List<Grounds> grounds) {
    }

    private void assignTimeToAllMatches(Tournaments tournament) {
    }

    private boolean roundRobinGeneration(long[] teamsId, long tournamentId) {
        int numberOfTeams = teamsId.length;
        long[] finalTeamsForRotation;

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
                Matches match = insertIntoMatches(tournamentId, round, matchNumber);
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
                    insertIntoVersus(teamsId[0], tournamentId, match.getMatchId());
                    insertIntoVersus(teamsId[teamIdx], tournamentId, match.getMatchId());
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
                        Matches nextMatch = insertIntoMatches(tournamentId, round, matchNumber);
                        insertIntoVersus(finalTeamsForRotation[firstTeam], tournamentId, nextMatch.getMatchId());
                        insertIntoVersus(finalTeamsForRotation[secondTeam], tournamentId, nextMatch.getMatchId());
                        matchNumber++;
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Matches insertIntoMatches(long tournamentId, int round, int matchNumber) {
            jdbcTemplate.update("insert into matches values(?,?,?,?,?,?,?,?,?,?,?,?,?)", null, tournamentId, null, null,
                    round, matchNumber, MatchStatus.UPCOMING.toString(), null, null, null, null, "false", null);
            return jdbcTemplate.query("SELECT * FROM matches ORDER BY matchId DESC LIMIT 1",
                    new BeanPropertyRowMapper<>(Matches.class)).get(0);
    }

    private void insertIntoVersus(long teamId, long tournamentId, long matchId) {
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
