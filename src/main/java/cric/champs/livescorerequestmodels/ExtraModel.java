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
    private boolean status = false;

    private int bye;

    private int legBye;

    private int wide;

    private int noBall;

}
