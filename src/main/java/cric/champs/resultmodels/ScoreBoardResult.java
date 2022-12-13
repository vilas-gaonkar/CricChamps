package cric.champs.resultmodels;

import cric.champs.entity.ScoreBoard;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.ExtraRuns;
import cric.champs.model.FallOfWicketSB;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScoreBoardResult {

    private ScoreBoard scoreBoard;

    private ExtraRuns extraRuns;

    private List<BatsmanSB> batsmanSB;

    private List<BowlerSB> bowlerSB;

    private List<FallOfWicketSB> fallOfWicketSB;

}
