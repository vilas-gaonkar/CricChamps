package cric.champs.livescorerequestmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnotherLiveScoreModel {

    private ExtraModel extraModel;

    private AnotherWicketModel wicketModel;

    private Long tournamentId;

    private Long matchId;

    private Long battingTeamId;

    private Long bowlingTeamId;

    private Long strikeBatsmanId;

    private Long nonStrikeBatsmanId;

    private Long bowlerId;

    private int over;

    private int ball;

    private int runs;

    private String overStatus;

    private String matchStatus;

}
