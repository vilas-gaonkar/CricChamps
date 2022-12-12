package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.entity.Matches;
import cric.champs.entity.Teams;
import cric.champs.entity.Tournaments;
import cric.champs.livescorerequestmodels.LiveScoreUpdate;
import cric.champs.model.Versus;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.fixture.FixtureGenerationInterface;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("ALL")
@Service
public class LiveScoreService implements LiveInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FixtureGenerationInterface fixtureGenerationInterface;

    @Autowired
    private SystemInterface systemInterface;

    private int numberOfOversOfTournament;

    @Override
    public SuccessResultModel updateLiveScore(LiveScoreUpdate liveScoreUpdateModel) throws LiveScoreUpdationException {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(liveScoreUpdateModel.getTournamentId());
        List<Matches> matches = systemInterface.verifyMatchId(liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId());
        List<Teams> strikeTeam = systemInterface.verifyTeamDetails(liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getTournamentId());
        if (tournament.isEmpty() || matches.isEmpty() || strikeTeam.isEmpty())
            throw new LiveScoreUpdationException("Invalid Tournament Updation");
        List<Versus> matchTeams = jdbcTemplate.query("select * from versus where matchId = ?",
                new BeanPropertyRowMapper<>(Versus.class), liveScoreUpdateModel.getMatchId());
        if (!matchTeams.contains(strikeTeam))
            throw new LiveScoreUpdationException("Invalid Team");

        numberOfOversOfTournament = tournament.get(0).getNumberOfOvers();


        return null;
    }

    private double getBattingStrikeRate(int runsScored, int numberOfBallFaced) {
        return runsScored / numberOfBallFaced * 100;
    }

    private double getBowlingStrikeRate(int numberBowledDeliveries, int numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? 0 : numberBowledDeliveries / numberOfWicketTaken;
    }

    private double getBattingAverage(int totalScoreOfBatsman, int numberOfTimesHeHasBeenOut) {
        return numberOfTimesHeHasBeenOut == 0 ? 0 : totalScoreOfBatsman / numberOfTimesHeHasBeenOut;
    }

    private double getBowlingAverage(int numberOfRunsConceded, int numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? 0 : numberOfRunsConceded / numberOfWicketTaken;
    }

    private double getBowlingEconomy(int numberOfRunsConceded, int numberOfOverBowled) {
        return numberOfRunsConceded / numberOfOverBowled;
    }

    private double currentRunRate(int numberOfOver, int totalRuns) {
        return totalRuns / numberOfOver;
    }

    private double netRunRatePlayed(int totalScoreInEveryMatch) {
        return totalScoreInEveryMatch / numberOfOversOfTournament;
    }

    private double netRunRateGiven(int totalScoreConceded) {
        return totalScoreConceded / numberOfOversOfTournament;
    }

    private double netRunRate(int totalScoreInEveryMatch, int totalScoreConceded) {
        return netRunRatePlayed(totalScoreInEveryMatch) - netRunRateGiven(totalScoreConceded);
    }

    private double requiredRunRate(int runNeededToWin, int remainingOvers) {
        return runNeededToWin / remainingOvers;
    }


}
