package cric.champs.livescorerequestmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WicketModel {

    private boolean wicketStatus = false;

    private String outType = null;

    private Long outPlayerId = null;

    private Long fielderId = null;

    private Long newBatsmanId = null;

}
