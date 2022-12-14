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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreBoardService implements ScoreboardInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public ScoreBoardResult viewScoreBoardResults(ScoreBoardModel scoreBoardModel) {
        List<ScoreBoard> scoreBoard = viewScoreBoard(scoreBoardModel);
        List<BatsmanSB> batsmanSB = viewBatsmanSB(scoreBoardModel);
        List<BowlerSB> bowlerSB = viewBowlerSB(scoreBoardModel);
        List<ExtraRuns> extraRuns = viewExtraRuns(scoreBoardModel);
        List<FallOfWicketSB> fallOfWicketSB = viewFallOfWickets(scoreBoardModel);
        return new ScoreBoardResult(scoreBoard, extraRuns, batsmanSB, bowlerSB, fallOfWicketSB);
    }

    @Override
    public List<ScoreBoard> viewScoreBoard(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
    }

    @Override
    public List<BatsmanSB> viewBatsmanSB(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from batsmanSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
    }

    @Override
    public List<BowlerSB> viewBowlerSB(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from bowlingSB where tournamentId = ? and teamId in (" +
                        "select teamId from versus where matchId = ? and teamId != ?)",
                new BeanPropertyRowMapper<>(BowlerSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
    }

    @Override
    public List<ExtraRuns> viewExtraRuns(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from extraRuns where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ExtraRuns.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
    }

    @Override
    public List<FallOfWicketSB> viewFallOfWickets(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from fallOfWicketSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(FallOfWicketSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
    }

}
