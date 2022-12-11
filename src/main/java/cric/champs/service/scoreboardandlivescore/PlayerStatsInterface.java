package cric.champs.service.scoreboardandlivescore;

import cric.champs.model.PlayerStats;

public interface PlayerStatsInterface {

    PlayerStats viewMostRuns(long tournamentId);

    PlayerStats viewBestBattingAverage(long tournamentId);

    PlayerStats viewBattingStrikeRate(long tournamentId);

    PlayerStats viewMostHundreds(long tournamentId);

    PlayerStats viewMostFifties(long tournamentId);

    PlayerStats viewMostFours(long tournamentId);

    PlayerStats viewMostSixes(long tournamentId);

    PlayerStats viewMostWicket(long tournamentId);

    PlayerStats viewBestBowlingAverage(long tournamentId);

    PlayerStats viewMostFiveWicket(long tournamentId);

    PlayerStats viewBestEconomy(long tournamentId);

    PlayerStats viewBestBowlingStrikeRate(long tournamentId);

    PlayerStats view(long tournamentId);

}
