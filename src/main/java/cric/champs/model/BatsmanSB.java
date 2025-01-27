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
public class BatsmanSB {

    private long scoreBoardId;

    private long teamId;

    private long matchId;

    private long playerId;

    private String playerName;

    private int runs;

    private int balls;

    private int fours;

    private int sixes;

    private double strikeRate;

    private String batsmanStatus;

    private String strikePosition;

    private String outByStatus;

    private String outByPlayer;
}
