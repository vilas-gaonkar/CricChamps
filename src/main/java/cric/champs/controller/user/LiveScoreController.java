package cric.champs.controller.user;

import cric.champs.entity.Live;
import cric.champs.livescorerequestmodels.LiveScoreModel;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.resultmodels.LiveScoreResult;
import cric.champs.resultmodels.ScoreBoardResult;
import cric.champs.service.scoreboardandlivescore.LiveResultInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/live")
public class LiveScoreController {

    @Autowired
    LiveResultInterface liveResultInterface;


    @PostMapping("/view-all")
    public ResponseEntity<LiveScoreResult> liveScoreResult(@RequestBody LiveScoreModel liveScoreModel) {
        return ResponseEntity.of(Optional.of(liveResultInterface.viewLiveScoreResult(liveScoreModel)));
    }

    @PostMapping("/view")
    public ResponseEntity<?> viewLive(@RequestBody LiveScoreModel liveScoreModel){
        return ResponseEntity.of(Optional.of(liveResultInterface.viewLiveScore(liveScoreModel)));
    }

    @PostMapping("/view-batsman")
    public ResponseEntity<?> viewBatsmanSB(@RequestBody LiveScoreModel liveScoreModel){
        return ResponseEntity.of(Optional.of(liveResultInterface.viewBatsmanSB(liveScoreModel)));
    }

    @PostMapping("/view-bowler")
    public ResponseEntity<?> viewBowlerSB(@RequestBody LiveScoreModel liveScoreModel){
        return ResponseEntity.of(Optional.of(liveResultInterface.viewBowlerSB(liveScoreModel)));
    }

    @PostMapping("/view-wickets")
    public ResponseEntity<?> viewFallOfWickets(@RequestBody LiveScoreModel liveScoreModel){
        return ResponseEntity.of(Optional.of(liveResultInterface.viewFallOfWickets(liveScoreModel)));
    }

    @PostMapping("/view-partnership")
    public ResponseEntity<?> viewPartnerShip(@RequestBody LiveScoreModel liveScoreModel){
        return ResponseEntity.of(Optional.of(liveResultInterface.viewPartnerShip(liveScoreModel)));
    }

    @PostMapping("/view-commentary")
    public ResponseEntity<?> viewCommentary(@RequestBody LiveScoreModel liveScoreModel){
        return ResponseEntity.of(Optional.of(liveResultInterface.viewCommentary(liveScoreModel)));
    }

}
