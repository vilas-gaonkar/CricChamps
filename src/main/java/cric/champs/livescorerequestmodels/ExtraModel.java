package cric.champs.livescorerequestmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExtraModel {
    private boolean extraStatus = false;

    private String extraType;

    private int bye;

    private int legBye;

    private int wide;

    private int noBall;

    private int penaltyRuns;

}
