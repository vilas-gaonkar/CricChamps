package cric.champs.controller.user;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdate;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.resultmodels.ScoreBoardResult;
import cric.champs.service.scoreboardandlivescore.LiveInterface;
import cric.champs.service.scoreboardandlivescore.ScoreboardInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/scoreboard")
public class ScoreBoardController {

    @Autowired
    private ScoreboardInterface scoreboardInterface;

    @Autowired
    LiveInterface liveInterface;

    @PostMapping("/view-all")
    public ResponseEntity<ScoreBoardResult> scoreBoardResult(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewScoreBoardResults(scoreBoardModel)));
    }

    //split apis
    @PostMapping("/")
    public ResponseEntity<?> viewScoreBoard(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewScoreBoard(scoreBoardModel)));
    }

    @PostMapping("/batsman-sb")
    public ResponseEntity<?> viewBatsmanSB(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewBatsmanSB(scoreBoardModel)));
    }

    @PostMapping("/bowler-sb")
    public ResponseEntity<?> viewBowlerSB(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewBowlerSB(scoreBoardModel)));
    }

    @PostMapping("/extra-sb")
    public ResponseEntity<?> viewExtraRuns(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewExtraRuns(scoreBoardModel)));
    }

    @PostMapping("/fall-of-wicket")
    public ResponseEntity<?> viewFallOfWicketSB(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewFallOfWickets(scoreBoardModel)));
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody LiveScoreUpdate liveScoreUpdate) throws LiveScoreUpdationException {
        return ResponseEntity.of(Optional.of(liveInterface.updateLiveScore(liveScoreUpdate)));
    }

}
