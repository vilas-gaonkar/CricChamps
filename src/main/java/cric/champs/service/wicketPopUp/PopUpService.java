package cric.champs.service.wicketPopUp;

import cric.champs.resultmodels.PlayersResult;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class PopUpService implements PopUpInterface{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public List<PlayersResult> remainingBatsman(long tournamentId, long matchId, long teamId) {
        return null;
    }

    @Override
    public List<PlayersResult> remainingBowlers(long tournamentId, long matchId, long teamId) {
        return null;
    }

    @Override
    public List<PlayersResult> Fielders(long tournamentId, long matchId, long teamId) {
        return null;
    }
}
