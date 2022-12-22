package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.resultmodels.SuccessResultModel;

public interface LiveScoreUpdateInterface {
    LiveScoreUpdateModel updateLiveScore(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException, FixtureGenerationException;

    SuccessResultModel stopMatch(long matchId, long tournamentId, String reason);
}
