package cric.champs.service.fixture;

import cric.champs.entity.Grounds;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Tournaments;
import cric.champs.entity.Umpires;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FixtureService implements FixtureGenerationInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public ResultModel generateFixture(long tournamentId) {
        Tournaments tournaments = systemInterface.verifyTournamentId(tournamentId).get(0);
        if(tournaments==null)
            return new ResultModel("Invalid tournament id");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ?",
                new BeanPropertyRowMapper<>(Grounds.class),tournaments.getTournamentId());
        List<Umpires> umpires = jdbcTemplate.query("select * from umpires where tournamentId = ?",
                new BeanPropertyRowMapper<>(Umpires.class),tournaments.getTournamentId());
        if(grounds.isEmpty()||umpires.isEmpty())
            return new ResultModel("please add ground or umpire");

    }
}
