package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.Live;
import cric.champs.model.LiveScoreModel;
import cric.champs.model.*;
import cric.champs.resultmodels.LiveScoreResult;
import cric.champs.resultmodels.SuccessResultModel;

import java.util.List;

public interface LiveResultInterface {

    LiveScoreResult viewLiveScoreResult(LiveScoreModel liveScoreModel);

    List<Live> viewLiveScore(LiveScoreModel liveScoreModel);

    List<BatsmanSB> viewBatsmanSB(LiveScoreModel liveScoreModel);

    BowlerSB viewBowlerSB(LiveScoreModel liveScoreModel);

    FallOfWicketSB viewFallOfWickets(LiveScoreModel liveScoreModel);

    Partnership viewPartnerShip(LiveScoreModel liveScoreModel);

    List<Commentary> viewCommentary(LiveScoreModel liveScoreModel);

    SuccessResultModel stopMatch(LiveScoreModel liveScoreModel, String reason);
}
