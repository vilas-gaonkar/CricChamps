package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.model.LiveScoreModel;
import cric.champs.resultmodels.SuccessResultModel;

public interface LiveScoreUpdateInterface {
    LiveScoreUpdateModel updateLiveScore(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException;

    SuccessResultModel stopMatch(LiveScoreModel liveScoreModel, String reason);
}
