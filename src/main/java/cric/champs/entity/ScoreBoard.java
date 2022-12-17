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

    private long teamId;

    private String teamName;

    private int overs;

    private int ball;

    private int score;

    private int totalWicketFall;

    private String matchStatus;
}
