package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.AnotherLiveScoreModel;

public interface AnotherLiveScoreInterface {
    AnotherLiveScoreModel updateLiveScore(AnotherLiveScoreModel liveScoreModel) throws LiveScoreUpdationException;
}
