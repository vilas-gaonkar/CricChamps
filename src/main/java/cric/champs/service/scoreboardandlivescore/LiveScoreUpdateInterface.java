package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.model.LiveScoreModel;
import cric.champs.resultmodels.SuccessResultModel;
import org.springframework.web.bind.annotation.RequestHeader;

public interface LiveScoreUpdateInterface {
    LiveScoreUpdateModel updateLiveScore(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException, FixtureGenerationException;

    SuccessResultModel stopMatch(long matchId, long tournamentId, String reason);
}
