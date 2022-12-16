package cric.champs.controller.user;

import cric.champs.model.ScoreBoardModel;
import cric.champs.model.Versus;
import cric.champs.resultmodels.ScoreBoardResult;
import cric.champs.service.scoreboardandlivescore.ScoreboardInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/scoreboard")
public class ScoreBoardController {

    @Autowired
    private ScoreboardInterface scoreboardInterface;


    @PostMapping("/view-all")
    public ResponseEntity<ScoreBoardResult> scoreBoardResult(@ModelAttribute ScoreBoardModel scoreBoardModel) {
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

    @GetMapping("/view-versus")
    public ResponseEntity<List<Versus>> viewVersus(@RequestHeader long tournamentId, @RequestHeader long matchId) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewMatchDetails(tournamentId, matchId)));
    }

}
