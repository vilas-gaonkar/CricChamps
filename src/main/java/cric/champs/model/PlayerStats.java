package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStats {

    private long playerId;

    private long tournamentId;

    private long teamId;

    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+$", message = "Player name should only contain alphabets")
    private String playerName;

    private double battingAverage;

    private double battingStrikeRate;

    private int totalFifties;

    private int totalHundreds;

    private int totalFours;

    private  int totalSixes;

    private int mostWickets;

    private int totalRuns;

    private double bestBowlingAverage;

    private double bestBowlingEconomy;

    private int mostFiveWicketsHaul;

}
