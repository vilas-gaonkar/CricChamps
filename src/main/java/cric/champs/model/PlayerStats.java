package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStats {

    private long playerId;

    private long tournamentId;

    private long teamId;

    private String playerName;

    private double battingAverage;

    private double battingStrikeRate;

    private int totalFifties;

    private int totalHundreds;

    private int totalFours;

    private  int totalSixes;

    private int mostWickets;

    private int mostRuns;

    private double bestBowlingAverage;

    private double bestBowlingEconomy;

    private int mostFiveWicketsHaul;

}
