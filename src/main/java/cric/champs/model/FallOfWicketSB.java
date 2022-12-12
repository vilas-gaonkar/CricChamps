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
public class FallOfWicketSB {

    private long scoreBoardId;

    private long tournamentId;

    private long matchId;

    private long teamId;

    private long playerId;

    private String playerName;

    private int score;

    private int wicketNumber;

    private int overs;

    private int balls;
}
