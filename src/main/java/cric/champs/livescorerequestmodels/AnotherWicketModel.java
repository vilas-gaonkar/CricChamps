package cric.champs.livescorerequestmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnotherWicketModel {

    private boolean wicketStatus = false;

    private String outType;

    private Long fielderId;

    private Long newBatsmanId;

}
