package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.Live;
import cric.champs.model.LiveScoreModel;
import cric.champs.model.*;
import cric.champs.resultmodels.LiveScoreResult;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.BatsmanStatus;
import cric.champs.service.BowlingStatus;
import cric.champs.service.MatchStatus;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return jdbcTemplate.query("select * from batsmanSB where teamId = ? and batsmanStatus = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), liveScoreModel.getMatchId(),
                BatsmanStatus.NOTOUT.toString());
    }

    @Override
    public BowlerSB viewBowlerSB(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from bowlingSB where teamId in (select teamId from versus where " +
                        "teamId != ? and matchId = ?) and bowlerStatus = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), liveScoreModel.getBattingTeamId(), liveScoreModel.getMatchId(),
                BowlingStatus.BOWLING.toString()).get(0);
    }

    @Override
    public FallOfWicketSB viewFallOfWickets(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<FallOfWicketSB> fallOfWicketSB = jdbcTemplate.query("select * from fallOfWicketSB where teamId = ?" +
                        "  order by wicketNumber DESC", new BeanPropertyRowMapper<>(FallOfWicketSB.class),
                liveScoreModel.getBattingTeamId());
        return fallOfWicketSB.isEmpty() ? null : fallOfWicketSB.get(0);
    }

    @Override
    public Partnership viewPartnerShip(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from partnership where teamId = ? order by partnershipId DESC",
                new BeanPropertyRowMapper<>(Partnership.class), liveScoreModel.getBattingTeamId()).get(0);
    }

    @Override
    public List<Commentary> viewCommentary(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from commentary where matchId = ? and tournamentId = ? order by commentaryId DESC",
                new BeanPropertyRowMapper<>(Commentary.class), liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
    }

    @Override
    public SuccessResultModel stopMatch(LiveScoreModel liveScoreModel, String reason) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        jdbcTemplate.update("update matches set matchStatus = ? , isCancelled = 'true' where tournamentId = ? and matchId = ?",
                MatchStatus.ABANDONED.toString(), liveScoreModel.getTournamentId(), liveScoreModel.getMatchId());
        return new SuccessResultModel("Match cancelled Successfully");
    }

}
