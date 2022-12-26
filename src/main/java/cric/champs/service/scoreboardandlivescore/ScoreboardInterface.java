package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.ScoreBoard;
import cric.champs.model.*;
import cric.champs.requestmodel.ScoreBoardModel;
import cric.champs.resultmodels.ScoreBoardResult;

import java.util.List;

public interface ScoreboardInterface {

    ScoreBoardResult viewScoreBoardResults(ScoreBoardModel scoreBoardModel);

    ScoreBoard viewScoreBoard(ScoreBoardModel scoreBoardModel);

    List<BatsmanSB> viewBatsmanSB(ScoreBoardModel scoreBoardModel);

    List<BowlerSB> viewBowlerSB(ScoreBoardModel scoreBoardModel);

    ExtraRuns viewExtraRuns(ScoreBoardModel scoreBoardModel);

    List<FallOfWicketSB> viewFallOfWickets(ScoreBoardModel scoreBoardModel);

}
