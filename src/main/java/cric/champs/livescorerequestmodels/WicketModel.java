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

    private boolean status = false;
    
    private Long tournamentId;

    private Long matchId;

    private Long teamId;

    private String outType;

    private Long batsmanId;

    private String batsman;

    private Long fielderId;

    private String fielderName;

    private Long new_batsmanId;

    private String new_batsman;

}
