package cric.champs.service.scoreboardandlivescore;

import cric.champs.entity.Live;
import cric.champs.livescorerequestmodels.LiveScoreModel;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.model.*;
import cric.champs.resultmodels.LiveScoreResult;
import cric.champs.resultmodels.LiveScoreUpdateResult;

import javax.swing.*;
import java.util.List;

public interface LiveResultInterface {

    LiveScoreResult viewLiveScoreResult(LiveScoreModel liveScoreModel);

    List<Live> viewLiveScore(LiveScoreModel liveScoreModel);

    List<BatsmanSB> viewBatsmanSB(LiveScoreModel liveScoreModel);

    BowlerSB viewBowlerSB(LiveScoreModel liveScoreModel);

    FallOfWicketSB viewFallOfWickets(LiveScoreModel liveScoreModel);

    Partnership viewPartnerShip(LiveScoreModel liveScoreModel);

    List<Commentary> viewCommentary(LiveScoreModel liveScoreModel);


}
