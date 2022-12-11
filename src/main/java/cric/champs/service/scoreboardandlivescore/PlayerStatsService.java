package cric.champs.service.scoreboardandlivescore;

import cric.champs.model.PlayerStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlayerStatsService implements PlayerStatsInterface{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public PlayerStats viewMostRuns(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewBestBattingAverage(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewBattingStrikeRate(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewMostHundreds(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewMostFifties(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewMostFours(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewMostSixes(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewMostWicket(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewBestBowlingAverage(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewMostFiveWicket(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewBestEconomy(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats viewBestBowlingStrikeRate(long tournamentId) {
        return null;
    }

    @Override
    public PlayerStats view(long tournamentId) {
        return null;
    }
}
