package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.Live;
import cric.champs.entity.Teams;
import cric.champs.livescorerequestmodels.LiveScoreModel;
import cric.champs.model.*;
import cric.champs.resultmodels.LiveScoreResult;
import cric.champs.service.BatsmanStatus;
import cric.champs.service.BowlingStatus;
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
    JdbcTemplate jdbcTemplate;

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
        if (systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from live where matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Live.class), liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
    }

    @Override
    public List<BatsmanSB> viewBatsmanSB(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from batsmanSB where teamId = ? and matchId = ? and batsmanStatus = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), liveScoreModel.getBattingTeamId(), liveScoreModel.getMatchId(),
                BatsmanStatus.NOTOUT.toString());
    }

    @Override
    public BowlerSB viewBowlerSB(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<Teams> teams = jdbcTemplate.query("select * from teams where teamId in (select teamId from versus where " +
                        "matchId = ? and teamId != ?)", new BeanPropertyRowMapper<>(Teams.class), liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        return jdbcTemplate.query("select * from bowlerSB where teamId = ? and matchId = ? and bowlerStatus = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), teams.get(0).getTeamId(), liveScoreModel.getMatchId(),
                BowlingStatus.BOWLING.toString()).get(0);
    }

    @Override
    public FallOfWicketSB viewFallOfWickets(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from fallOfWicketSB where teamId = ? and matchId = ? order by wicketNumber DESC",
                new BeanPropertyRowMapper<>(FallOfWicketSB.class), liveScoreModel.getBattingTeamId(), liveScoreModel.getMatchId()).get(0);
    }

    @Override
    public Partnership viewPartnerShip(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from partnership where teamId = ? order by partnershipId DESC",
                new BeanPropertyRowMapper<>(Partnership.class), liveScoreModel.getBattingTeamId()).get(0);
    }

    @Override
    public List<Commentary> viewCommentary(LiveScoreModel liveScoreModel) {
        if (systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from commentary where matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Commentary.class), liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
    }
}
