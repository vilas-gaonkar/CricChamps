package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.Live;
import cric.champs.requestmodel.LiveScoreModel;
import cric.champs.model.*;
import cric.champs.resultmodels.LiveScoreResult;

import java.util.List;

public interface LiveResultInterface {

    LiveScoreResult viewLiveScoreResult(LiveScoreModel liveScoreModel);

    List<Live> viewLiveScore(LiveScoreModel liveScoreModel);

    List<BatsmanSB> viewBatsmanSB(LiveScoreModel liveScoreMo);

    BowlerSB viewBowlerSB(LiveScoreModel liveScoreModel);

    FallOfWicketSB viewFallOfWickets(LiveScoreModel liveScoreModel);

    Partnership viewPartnerShip(LiveScoreModel liveScoreModel);

    List<Commentary> viewCommentary(LiveScoreModel liveScoreModel);

}
