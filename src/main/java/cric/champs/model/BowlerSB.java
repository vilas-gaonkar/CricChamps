package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BowlerSB {

    private long scoreBoardId;

    private long tournamentId;

    private long matchId;

    private long teamId;

    private long playerId;

    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+$", message = "Player name should only contain alphabets")
    private String playerName;

    private int runs;

    private int overs;

    private int balls;

    private int maidenOvers;

    private int wickets;

    private double economyRate;
}
