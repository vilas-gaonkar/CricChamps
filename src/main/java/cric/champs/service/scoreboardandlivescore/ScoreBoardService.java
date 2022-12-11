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
        ScoreBoard scoreBoard = viewScoreBoard(scoreBoardModel);
        BatsmanSB batsmanSB = viewBatsmanSB(scoreBoardModel);
        BowlerSB bowlerSB = viewBowlerSB(scoreBoardModel);
        ExtraRuns extraRuns = viewExtraRuns(scoreBoardModel);
        FallOfWicketSB fallOfWicketSB = viewFallOfWickets(scoreBoardModel);
        return new ScoreBoardResult(scoreBoard, extraRuns, batsmanSB, bowlerSB, fallOfWicketSB);
    }

    @Override
    public ScoreBoard viewScoreBoard(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId()).get(0);
    }

    @Override
    public BatsmanSB viewBatsmanSB(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from batsmanSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId()).get(0);
    }

    @Override
    public BowlerSB viewBowlerSB(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from bowlerSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId()).get(0);
    }

    @Override
    public ExtraRuns viewExtraRuns(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from bowlerSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ExtraRuns.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId()).get(0);
    }

    @Override
    public FallOfWicketSB viewFallOfWickets(ScoreBoardModel scoreBoardModel) {
        if (systemInterface.verifyTournamentId(scoreBoardModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from bowlerSB where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(FallOfWicketSB.class), scoreBoardModel.getTournamentId(), scoreBoardModel.getMatchId(),
                scoreBoardModel.getTeamId()).get(0);
    }

}
