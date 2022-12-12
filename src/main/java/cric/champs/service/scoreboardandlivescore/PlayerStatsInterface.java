package cric.champs.service.scoreboardandlivescore;

import cric.champs.model.PlayerStats;

import java.util.List;

public interface PlayerStatsInterface {

    List<PlayerStats> viewMostRuns(long tournamentId);

    List<PlayerStats> viewBestBattingAverage(long tournamentId);

    List<PlayerStats> viewBattingStrikeRate(long tournamentId);

    List<PlayerStats> viewMostHundreds(long tournamentId);

    List<PlayerStats> viewMostFifties(long tournamentId);

    List<PlayerStats> viewMostFours(long tournamentId);

    List<PlayerStats> viewMostSixes(long tournamentId);

    List<PlayerStats> viewMostWicket(long tournamentId);

    List<PlayerStats> viewBestBowlingAverage(long tournamentId);

    List<PlayerStats> viewMostFiveWicket(long tournamentId);

    List<PlayerStats> viewBestEconomy(long tournamentId);

    List<PlayerStats> viewBestBowlingStrikeRate(long tournamentId);

    List<PlayerStats> viewHighestScoreTeam(long tournamentId);

    List<PlayerStats> viewPlayerStats(String playerStatsField, long tournamentId);

}
