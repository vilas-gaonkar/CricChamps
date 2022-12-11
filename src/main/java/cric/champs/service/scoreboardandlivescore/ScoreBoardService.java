package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.ScoreBoard;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.ExtraRuns;
import cric.champs.model.FallOfWicketSB;
import cric.champs.resultmodels.ScoreBoardResult;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class ScoreBoardService implements ScoreboardInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ScoreBoardResult viewScoreBoardResults(ScoreBoardModel scoreBoardModel) {
        ScoreBoard scoreBoard = viewScoreBoard(scoreBoardModel);
        BatsmanSB batsmanSB = viewBatsmanSB(scoreBoardModel);
        BowlerSB bowlerSB = viewBowlerSB(scoreBoardModel);
        ExtraRuns extraRuns = viewExtraRuns(scoreBoardModel);
        FallOfWicketSB fallOfWicketSB = viewFallOfWickets(scoreBoardModel);
        return new ScoreBoardResult(scoreBoard, extraRuns, batsmanSB, bowlerSB, fallOfWicketSB);
    }

    @Override
    public ScoreBoard viewScoreBoard(ScoreBoardModel scoreBoardModel) {
        List<ScoreBoard> scoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
        if (scoreBoard.isEmpty())
            throw new NullPointerException("Invalid tournament");
        return scoreBoard.get(0);
    }

    @Override
    public BatsmanSB viewBatsmanSB(ScoreBoardModel scoreBoardModel) {
        List<BatsmanSB> batsmanSB = jdbcTemplate.query("select * from batsmanSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
        if (batsmanSB.isEmpty())
            throw new NullPointerException("Invalid tournament");
        return batsmanSB.get(0);
    }

    @Override
    public BowlerSB viewBowlerSB(ScoreBoardModel scoreBoardModel) {
        List<BowlerSB> bowlerSB = jdbcTemplate.query("select * from bowlerSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
        if (bowlerSB.isEmpty())
            throw new NullPointerException("Invalid tournament");
        return bowlerSB.get(0);
    }

    @Override
    public ExtraRuns viewExtraRuns(ScoreBoardModel scoreBoardModel) {
        List<ExtraRuns> extraRuns = jdbcTemplate.query("select * from bowlerSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ExtraRuns.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
        if (extraRuns.isEmpty())
            throw new NullPointerException("Invalid tournament");
        return extraRuns.get(0);
    }

    @Override
    public FallOfWicketSB viewFallOfWickets(ScoreBoardModel scoreBoardModel) {
        List<FallOfWicketSB> fallOfWicketSB = jdbcTemplate.query("select * from bowlerSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(FallOfWicketSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
        if (fallOfWicketSB.isEmpty())
            throw new NullPointerException("Invalid tournament");
        return fallOfWicketSB.get(0);
    }

}
