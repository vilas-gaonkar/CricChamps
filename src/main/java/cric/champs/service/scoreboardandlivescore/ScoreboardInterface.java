package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.ScoreBoard;
import cric.champs.model.*;
import cric.champs.resultmodels.ScoreBoardResult;

import java.util.List;

public interface ScoreboardInterface {

    ScoreBoardResult viewScoreBoardResults(ScoreBoardModel scoreBoardModel);

    List<ScoreBoard> viewScoreBoard(ScoreBoardModel scoreBoardModel);

    List<BatsmanSB> viewBatsmanSB(ScoreBoardModel scoreBoardModel);

    List<BowlerSB> viewBowlerSB(ScoreBoardModel scoreBoardModel);

    ExtraRuns viewExtraRuns(ScoreBoardModel scoreBoardModel);

    List<FallOfWicketSB> viewFallOfWickets(ScoreBoardModel scoreBoardModel);

    List<Versus> viewMatchDetails(long tournamentId, long matchId);

}
