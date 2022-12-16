package cric.champs.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExtraRuns {

    private long scoreBoardId;

    private long teamId;

    private int noBall;

    private int wide;

    private int legBye;

    private int bye;

    private int penaltyRuns;

    private int totalExtraRuns;
}
