package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.ScoreBoard;
import cric.champs.model.*;
import cric.champs.requestmodel.ScoreBoardModel;
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
        ScoreBoard scoreBoard = viewScoreBoard(scoreBoardModel);
        List<BatsmanSB> batsmanSB = viewBatsmanSB(scoreBoardModel);
        List<BowlerSB> bowlerSB = viewBowlerSB(scoreBoardModel);
        ExtraRuns extraRuns = viewExtraRuns(scoreBoardModel);
        List<FallOfWicketSB> fallOfWicketSB = viewFallOfWickets(scoreBoardModel);
        return new ScoreBoardResult(scoreBoard, extraRuns, batsmanSB, bowlerSB, fallOfWicketSB);
    }

    @Override
    public ScoreBoard viewScoreBoard(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ScoreBoard> scoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId());
        return scoreBoard.isEmpty() ? null : scoreBoard.get(0);
    }

    @Override
    public List<BatsmanSB> viewBatsmanSB(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from batsmanSB where scoreBoardId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), systemInterface.getScoreBoardId(scoreBoardModel),
                scoreBoardModel.getTeamId());
    }

    @Override
    public List<BowlerSB> viewBowlerSB(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from bowlingSB where scoreBoardId = ? and teamId in (" +
                        "select teamId from versus where matchId = ? and teamId != ?)",
                new BeanPropertyRowMapper<>(BowlerSB.class), systemInterface.getScoreBoardId(scoreBoardModel),
                scoreBoardModel.getMatchId(), scoreBoardModel.getTeamId());
    }

    @Override
    public ExtraRuns viewExtraRuns(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ExtraRuns> extraRuns = jdbcTemplate.query("select * from extraRuns where scoreBoardId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ExtraRuns.class), systemInterface.getScoreBoardId(scoreBoardModel),
                scoreBoardModel.getTeamId());
        return extraRuns.isEmpty() ? null : extraRuns.get(0);
    }

    @Override
    public List<FallOfWicketSB> viewFallOfWickets(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from fallOfWicketSB where scoreBoardId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(FallOfWicketSB.class), systemInterface.getScoreBoardId(scoreBoardModel),
                scoreBoardModel.getTeamId());
    }

}
