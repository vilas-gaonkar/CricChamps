package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Versus {

    private long matchId;

    private long teamId;

    private String teamName;

    private int totalScore;

    private int totalWickets;

    private int totalOverPlayed;

    private int totalBallsPlayed;

    private String matchResult;

    private String isCancelled;
}
