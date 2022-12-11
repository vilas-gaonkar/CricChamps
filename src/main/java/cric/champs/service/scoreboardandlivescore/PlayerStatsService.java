package cric.champs.service.scoreboardandlivescore;

import cric.champs.model.PlayerStats;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerStatsService implements PlayerStatsInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    private final int limit = 5;

    @Override
    public List<PlayerStats> viewMostRuns(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by mostRuns DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewBestBattingAverage(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by battingAverage DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewBattingStrikeRate(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by battingStrikeRate DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewMostHundreds(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by totalHundreds DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewMostFifties(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by totalFifties DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewMostFours(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by totalFours DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewMostSixes(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by totalSixes DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewMostWicket(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by mostWickets DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewBestBowlingAverage(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by bestBowlingAverage DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewMostFiveWicket(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by mostFiveWicketsHaul DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewBestEconomy(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by bestBowlingEconomy DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewBestBowlingStrikeRate(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from playerStats where tournamentId = ? order by bestBowlingStrikeRate DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

    @Override
    public List<PlayerStats> viewHighestScoreTeam(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from players where tournamentId = ? order by teamHighestScore DESC limit ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), tournamentId, limit);
    }

}
