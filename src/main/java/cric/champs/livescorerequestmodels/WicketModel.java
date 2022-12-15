package cric.champs.livescorerequestmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WicketModel {

    private boolean wicketStatus = false;
    
    private Long tournamentId;

    private Long matchId;

    private Long teamId;

    private String outType;

    private Long batsmanId;

    private Long nonStrikeBatsmanId;

    private Long bowlerId;

    private Long fielderId;

    private int runs;

    private int over;

    private  int ball;

    private Long newBatsmanId;

    private ExtraModel extraModel;

}
