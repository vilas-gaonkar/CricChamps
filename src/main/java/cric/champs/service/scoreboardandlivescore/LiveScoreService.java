package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.entity.*;
import cric.champs.livescorerequestmodels.LiveScoreUpdate;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.MatchStatus;
import cric.champs.service.TournamentStatus;
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
        if (tournament.isEmpty() || matches.isEmpty())
            throw new LiveScoreUpdationException("Invalid Tournament Updation");
        if (liveScoreUpdateModel.getBall() > 6 && liveScoreUpdateModel.getBall() < 0)
            throw new LiveScoreUpdationException("Invalid ball");
        if (liveScoreUpdateModel.getOver() > tournament.get(0).getNumberOfOvers() && liveScoreUpdateModel.getOver() < 0)
            throw new LiveScoreUpdationException("Inavlid over");
        if (liveScoreUpdateModel.getRuns() > 7 && liveScoreUpdateModel.getRuns() < 0)
            throw new LiveScoreUpdationException("Invalid runs");
        List<Teams> strikeTeam = systemInterface.verifyTeamDetails(liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getTournamentId());
        List<Teams> nonStrikeTeam = jdbcTemplate.query("select * from teams where teamId in (select teamId from versus matchId = ? and teamId != ?)",
                new BeanPropertyRowMapper<>(Teams.class), liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId());
        if (strikeTeam.isEmpty() || nonStrikeTeam.isEmpty())
            throw new LiveScoreUpdationException("Invalid Team");
        List<Players> strikePlayer = jdbcTemplate.query("select * from players where playerId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(Players.class), liveScoreUpdateModel.getStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId());
        List<Players> nonStrikePlayer = jdbcTemplate.query("select * from players where playerId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(Players.class), liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId());
        List<Players> bowlingPlayer = jdbcTemplate.query("select * from players where playerId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(Players.class), liveScoreUpdateModel.getBowlerId(), nonStrikeTeam.get(0).getTeamId());
        if (strikePlayer.isEmpty() || nonStrikePlayer.isEmpty() || bowlingPlayer.isEmpty())
            throw new LiveScoreUpdationException("Invalid player");
        numberOfOversOfTournament = tournament.get(0).getNumberOfOvers();
        Long scoreBoardId = null;
        if (liveScoreUpdateModel.getOver() == 0 && (liveScoreUpdateModel.getBall() == 1 || (liveScoreUpdateModel.getExtraModel().isExtraStatus() && liveScoreUpdateModel.getBall() == 0)))
            scoreBoardId = setStatus(liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), strikeTeam.get(0).getTeamName());
        updateScoreBoard(tournament.get(0), matches.get(0), nonStrikeTeam, strikeTeam.get(0), liveScoreUpdateModel,scoreBoardId);
        updateLiveScoreAndCommentry(tournament.get(0), matches.get(0), nonStrikeTeam, strikeTeam.get(0), liveScoreUpdateModel);
        return null;
    }

    private Long setStatus(Long tournamentId, Long matchId, Long teamId, String teamName) {
        jdbcTemplate.update("update matches set matchStatus = ? where matchId = ? and tournamentId = ?", MatchStatus.LIVE.toString(),
                matchId, tournamentId);
        jdbcTemplate.update("update tournaments set tournamentStatus = ? where tournamentId = ?", TournamentStatus.PROGRESS.toString(),
                tournamentId);
        jdbcTemplate.update("insert into scoreBoard values(?,?,?,?,?,?,?,?,?)", null, tournamentId, matchId,
                teamId, teamName, 0, 0, 0, 0);
        return jdbcTemplate.query("select * from scoreBoard order by scoreBoardId DESC limit 1",
                new BeanPropertyRowMapper<>(ScoreBoard.class)).get(0).getScoreBoardId();
    }

    private void updateLiveScoreAndCommentry(Tournaments tournaments, Matches matches, List<Teams> matchTeams, Teams teams, LiveScoreUpdate liveScoreUpdateModel) {
        List<Live> lives = jdbcTemplate.query("select * from live where teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Live.class),liveScoreUpdateModel.getBattingTeamId(),liveScoreUpdateModel.getMatchId(),
                liveScoreUpdateModel.getTournamentId());
        if(lives.isEmpty()){
            if (liveScoreUpdateModel.getExtraModel().isExtraStatus()){
                jdbcTemplate.update("insert into live values(?,?,?,?,?,?,?,)", null, liveScoreUpdateModel.getTournamentId(),
                        liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), matchTeams.get(0).getTeamName(),
                        0, 0);
                jdbcTemplate.update("");
            }

        }

    }

    private void updateScoreBoard(Tournaments tournaments, Matches matches, List<Teams> matchTeams, Teams teams, LiveScoreUpdate liveScoreUpdateModel,Long scoreBoardId) {
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
            jdbcTemplate.update("insert into scoreBoard values(");
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
