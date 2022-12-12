package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdate;
import cric.champs.resultmodels.SuccessResultModel;

public interface LiveInterface {
    SuccessResultModel updateLiveScore(LiveScoreUpdate liveScoreUpdateModel) throws LiveScoreUpdationException;
}
