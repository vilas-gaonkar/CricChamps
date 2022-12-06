package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScoreBoard {

    private long scoreBoardId;

    private long tournamentId;

    private long matchId;

    private String teamName;

    private String tossStatus;

    private String tossDecision;

    private int currentScore;

    private int currentFallOfWickets;

    private int currentOver;

    private int currentBall;
}
