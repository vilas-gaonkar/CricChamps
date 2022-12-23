package cric.champs.service.wicketPopUp;

import cric.champs.entity.ScoreBoard;
import cric.champs.resultmodels.PlayersResult;
import cric.champs.service.OverStatus;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PopUpService implements PopUpInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public List<PlayersResult> remainingBatsman(long tournamentId, long matchId, long teamId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ScoreBoard> scoreBoards = getScoreBoard(matchId, teamId);
        return jdbcTemplate.query("select players.playerId, players.playerName from players where not exists " +
                        "(select batsmanSB.playerId from batsmanSB where players.playerId = batsmanSB.playerId " +
                        "and matchId = ?) and teamId = ?", new BeanPropertyRowMapper<>(PlayersResult.class),
                matchId, teamId);
    }

    private List<ScoreBoard> getScoreBoard(long matchId, long teamId) {
        return jdbcTemplate.query("select * from scoreBoard where teamId = ? and matchId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), teamId, matchId);
    }

    @Override
    public List<PlayersResult> remainingBowlers(long tournamentId, long matchId, long teamId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<ScoreBoard> scoreBoards = getScoreBoard(matchId, teamId);
        return jdbcTemplate.query("select players.playerId, players.playerName from players where not exists " +
                        "(select bowlingSB.playerId from bowlingSB where players.playerId = bowlingSB.playerId and " +
                        "bowlingSB.bowlerStatus = ? and matchId = ?) and teamId = ?",
                new BeanPropertyRowMapper<>(PlayersResult.class), OverStatus.DONE.toString(),
                matchId, teamId);
    }

    @Override
    public List<PlayersResult> fielders(long tournamentId, long matchId, long teamId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select playerId, playerName from players where teamId = ?",
                new BeanPropertyRowMapper<>(PlayersResult.class), teamId);
    }
}
