package cric.champs.service.fixture;

import cric.champs.entity.Matches;
import cric.champs.model.Versus;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchService implements MatchInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public List<Matches> viewAllMatches(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = 'false'",
                new BeanPropertyRowMapper<>(Matches.class), tournamentId);
    }

    @Override
    public List<Versus> viewMatchDetails(long tournamentId, long matchId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from versus where matchId = ? ",
                new BeanPropertyRowMapper<>(Versus.class), matchId);
    }
}
