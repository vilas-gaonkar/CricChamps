package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.Live;
import cric.champs.entity.ScoreBoard;
import cric.champs.requestmodel.LiveScoreModel;
import cric.champs.model.*;
import cric.champs.resultmodels.LiveScoreResult;
import cric.champs.service.BatsmanStatus;
import cric.champs.service.BowlingStatus;
import cric.champs.service.MatchStatus;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class LiveScoreResultService implements LiveResultInterface {

    @Autowired
    private SystemInterface systemInterface;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public LiveScoreResult viewLiveScoreResult(LiveScoreModel liveScoreModel) {
        List<Live> lives = viewLiveScore(liveScoreModel);
        List<BatsmanSB> batsmanSB = viewBatsmanSB(liveScoreModel);
        BowlerSB bowlerSB = viewBowlerSB(liveScoreModel);
        FallOfWicketSB fallOfWicketSB = viewFallOfWickets(liveScoreModel);
        Partnership partnership = viewPartnerShip(liveScoreModel);
        List<Commentary> commentaries = viewCommentary(liveScoreModel);
        return new LiveScoreResult(lives, batsmanSB, bowlerSB, partnership, fallOfWicketSB, commentaries);
    }

    @Override
    public List<Live> viewLiveScore(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from live where matchId = ? and tournamentId = ? order by liveId DESC",
                new BeanPropertyRowMapper<>(Live.class), liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
    }

    @Override
    public List<BatsmanSB> viewBatsmanSB(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ScoreBoard> scoreBoard = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId());
        if (scoreBoard.isEmpty())
            return null;
        return jdbcTemplate.query("select * from batsmanSB where scoreBoardId = ? and batsmanStatus = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), scoreBoard.get(0).getScoreBoardId(),
                BatsmanStatus.NOTOUT.toString());
    }

    private List<ScoreBoard> getScoreBoardId(Long tournamentId, Long matchId) {
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and " +
                        "(matchStatus = ? or matchStatus = ? or matchStatus = ?)",
                new BeanPropertyRowMapper<>(ScoreBoard.class), tournamentId, matchId, MatchStatus.INPROGRESS.toString(),
                MatchStatus.FIRSTINNING.toString(), MatchStatus.SECONDINNING.toString());
    }

    @Override
    public BowlerSB viewBowlerSB(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ScoreBoard> scoreBoard = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId());
        if (scoreBoard.isEmpty())
            return null;
        List<BowlerSB> bowlerSB = jdbcTemplate.query("select * from bowlingSB where scoreBoardId = ?  and bowlerStatus = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), scoreBoard.get(0).getScoreBoardId(),
                BowlingStatus.BOWLING.toString());
        return bowlerSB.isEmpty() ? null : bowlerSB.get(0);
    }

    @Override
    public FallOfWicketSB viewFallOfWickets(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ScoreBoard> scoreBoard = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId());
        if (scoreBoard.isEmpty())
            return null;
        List<FallOfWicketSB> fallOfWicketSB = jdbcTemplate.query("select * from fallOfWicketSB where scoreBoardId = ?" +
                        "  order by wicketNumber DESC", new BeanPropertyRowMapper<>(FallOfWicketSB.class),
                scoreBoard.get(0).getScoreBoardId());
        return fallOfWicketSB.isEmpty() ? null : fallOfWicketSB.get(0);
    }

    @Override
    public Partnership viewPartnerShip(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<Partnership> partnerships = jdbcTemplate.query("select * from partnership where  matchId = ? and tournamentId = ? order by partnershipId DESC",
                new BeanPropertyRowMapper<>(Partnership.class), liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
        return partnerships.isEmpty() ? null : partnerships.get(0);

    }

    @Override
    public List<Commentary> viewCommentary(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from commentary where matchId = ? and tournamentId = ? order by commentaryId DESC",
                new BeanPropertyRowMapper<>(Commentary.class), liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
    }

}
