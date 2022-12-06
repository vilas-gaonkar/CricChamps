package cric.champs.service.fixture;

import cric.champs.entity.*;
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
    public ResultModel generateFixture(long tournamentId) {
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
        int numberOfTournamentDays = Period.between(tournament.getTournamentStartDate().toLocalDate(), tournament.getTournamentEndDate().toLocalDate()).getDays();

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
            return new ResultModel("Cannot generate the fixture for tournament please provide more ground or decrease the overs");
        else
            generateFixture(tournament, grounds, umpires);

        return null;
    }

    private void generateFixture(Tournaments tournament, List<Grounds> grounds, List<Umpires> umpires) {
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted='false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId());

        long[] teamsId = new long[teams.size()];
        int index = 0;
        for (Teams team : teams) {
            teamsId[index] = team.getTeamId();
            index++;
        }
        boolean result = roundRobinGeneration(teamsId);

    }

    private boolean roundRobinGeneration(long[] teams) {
        int numberOfTeams = teams.length;
        long[] finalTeamsForRotation;

        if (numberOfTeams % 2 == 0) {
            finalTeamsForRotation = new long[numberOfTeams - 1];
            System.arraycopy(teams, 1, finalTeamsForRotation, 0, numberOfTeams - 1);
        } else {
            finalTeamsForRotation = new long[numberOfTeams];
            System.arraycopy(teams, 1, finalTeamsForRotation, 0, numberOfTeams - 1);
            finalTeamsForRotation[numberOfTeams - 1] = 0;
        }
        int totalRounds = finalTeamsForRotation.length; //it is even number
        int halfSize = (totalRounds + 1) / 2;
        int round = 1;
        for (int rounds = totalRounds; rounds > 0; rounds--) {
            //insert into match
            System.out.println("week " + (round++));
            jdbcTemplate.update("insert into matches values(");
            int teamIdx = rounds % totalRounds;

            //insert into teams
            if (finalTeamsForRotation[teamIdx] != 0) {
                System.out.println(teams[0] + " vs. " + finalTeamsForRotation[teamIdx]);
            }
            for (int i = 1; i < halfSize; i++) {
                int firstTeam = (rounds + i) % totalRounds;
                int secondTeam = (rounds + totalRounds - i) % totalRounds;
                if (finalTeamsForRotation[firstTeam] != 0 && finalTeamsForRotation[secondTeam] != 0) {
                    System.out.println(finalTeamsForRotation[firstTeam] + " vs. " + finalTeamsForRotation[secondTeam]);
                }
            }
            System.out.println();
        }
        return false;
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
