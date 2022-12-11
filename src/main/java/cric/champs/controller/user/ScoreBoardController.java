package cric.champs.controller.user;

import cric.champs.entity.ScoreBoard;
import cric.champs.livescorerequestmodels.ScoreBoardModel;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.ExtraRuns;
import cric.champs.model.FallOfWicketSB;
import cric.champs.resultmodels.ScoreBoardResult;
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

    @PostMapping("/view-all")
    public ResponseEntity<ScoreBoardResult> scoreBoardResult(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewScoreBoardResults(scoreBoardModel)));
    }

    @PostMapping("/")
    public ResponseEntity<ScoreBoard> viewScoreBoard(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewScoreBoard(scoreBoardModel)));
    }

    @PostMapping("/batsman-sb")
    public ResponseEntity<BatsmanSB> viewBatsmanSB(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewBatsmanSB(scoreBoardModel)));
    }

    @PostMapping("/bowler-sb")
    public ResponseEntity<BowlerSB> viewBowlerSB(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewBowlerSB(scoreBoardModel)));
    }

    @PostMapping("/extra-sb")
    public ResponseEntity<ExtraRuns> viewExtraRuns(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewExtraRuns(scoreBoardModel)));
    }

    @PostMapping("/fall-of-wicket")
    public ResponseEntity<FallOfWicketSB> viewFallOfWicketSB(@RequestBody ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewFallOfWickets(scoreBoardModel)));
    }

}