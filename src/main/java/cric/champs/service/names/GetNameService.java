package cric.champs.service.names;

import cric.champs.entity.Grounds;
import cric.champs.entity.Teams;
import cric.champs.entity.Tournaments;
import cric.champs.entity.Umpires;
import cric.champs.model.GroundPhotos;
import cric.champs.resultmodels.NameResult;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetNameService implements GetNamesInterface {

    @Autowired
    private SystemInterface systemInterface;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<NameResult> getAllUserTournamentName() {
        List<Tournaments> tournaments = systemInterface.verifyUserID();
        if (tournaments.isEmpty())
            return new ArrayList<>();
        List<NameResult> nameResult = new ArrayList<>();
        for (Tournaments tournament : tournaments)
            nameResult.add(new NameResult(tournament.getTournamentId(), tournament.getTournamentName(),
                    tournament.getTournamentLogo()));
        return nameResult;
    }

    @Override
    public List<NameResult> getAllTeamNames(long tournamentId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Teams.class), tournamentId);
        List<NameResult> nameResult = new ArrayList<>();
        for (Teams team : teams)
            nameResult.add(new NameResult(team.getTeamId(), team.getTeamName(), team.getTeamLogo()));
        return nameResult;
    }

    @Override
    public List<NameResult> getAllGroundsName(long tournamentId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Grounds.class), tournamentId);
        List<NameResult> nameResult = new ArrayList<>();
        for (Grounds ground : grounds)
            nameResult.add(new NameResult(ground.getGroundId(), ground.getGroundName(),
                    getGroundPics(ground.getGroundId()).get(0).getGroundPhoto()));
        return nameResult;
    }

    private List<GroundPhotos> getGroundPics(long groundId) {
        return jdbcTemplate.query("select * from groundPhotos where groundId = ?",
                new BeanPropertyRowMapper<>(GroundPhotos.class), groundId);
    }

    @Override
    public List<NameResult> getAllUmpiresName(long tournamentId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        List<Umpires> umpires = jdbcTemplate.query("select * from umpires where tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Umpires.class), tournamentId);
        List<NameResult> nameResult = new ArrayList<>();
        for (Umpires umpire : umpires)
            nameResult.add(new NameResult(umpire.getUmpireId(), umpire.getUmpireName(), umpire.getUmpirePhoto()));
        return nameResult;
    }

}
