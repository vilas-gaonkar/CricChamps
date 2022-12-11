package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.ScoreBoard;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.ExtraRuns;
import cric.champs.model.FallOfWicketSB;
import cric.champs.resultmodels.ScoreBoardResult;

public interface ScoreboardInterface {

    ScoreBoardResult viewScoreBoardResults(ScoreBoardModel scoreBoardModel);

    ScoreBoard viewScoreBoard(ScoreBoardModel scoreBoardModel);

    BatsmanSB viewBatsmanSB(ScoreBoardModel scoreBoardModel);

    BowlerSB viewBowlerSB(ScoreBoardModel scoreBoardModel);

    ExtraRuns viewExtraRuns(ScoreBoardModel scoreBoardModel);

    FallOfWicketSB viewFallOfWickets(ScoreBoardModel scoreBoardModel);

}
