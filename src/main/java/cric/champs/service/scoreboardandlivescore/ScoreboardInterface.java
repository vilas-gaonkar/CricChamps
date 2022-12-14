package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.ScoreBoard;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.ExtraRuns;
import cric.champs.model.FallOfWicketSB;
import cric.champs.resultmodels.ScoreBoardResult;

import java.util.List;

public interface ScoreboardInterface {

    ScoreBoardResult viewScoreBoardResults(ScoreBoardModel scoreBoardModel);

    List<ScoreBoard> viewScoreBoard(ScoreBoardModel scoreBoardModel);

    List<BatsmanSB> viewBatsmanSB(ScoreBoardModel scoreBoardModel);

    List<BowlerSB> viewBowlerSB(ScoreBoardModel scoreBoardModel);

    List<ExtraRuns> viewExtraRuns(ScoreBoardModel scoreBoardModel);

    List<FallOfWicketSB> viewFallOfWickets(ScoreBoardModel scoreBoardModel);

}
