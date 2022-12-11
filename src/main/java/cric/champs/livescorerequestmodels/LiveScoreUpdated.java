package cric.champs.livescorerequestmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveScoreUpdated {

    private Long tournamentId;

    private Long matchId;

    private Long playingTeamId;

    private Long strikeBatsmanId;

    private String nonStrikeBatsmanId;

    private Long bowlerId;

    private String bowlerName;

    private int over;

    private int balls;

    private int run;

    private ExtraModel extraModel;



}
