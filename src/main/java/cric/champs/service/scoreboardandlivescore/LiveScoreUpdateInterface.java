package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;

public interface LiveScoreUpdateInterface {
    LiveScoreUpdateModel updateLiveScore(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException;
}
