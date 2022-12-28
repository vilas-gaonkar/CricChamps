package cric.champs.resultmodels;

import cric.champs.entity.Live;
import cric.champs.entity.ScoreBoard;
import cric.champs.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveScoreResult {

    private List<Live> lives;

    private List<BatsmanSB> batsmanSB;

    private BowlerSB bowlerSB;

    private Partnership partnership;

    private FallOfWicketSB fallOfWicketSB;

    private List<Commentary> commentary;

    private String matchStatus;
}
