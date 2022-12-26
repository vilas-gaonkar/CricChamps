package cric.champs.controller.user;

import cric.champs.entity.ScoreBoard;
import cric.champs.model.*;
import cric.champs.requestmodel.ScoreBoardModel;
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
    @GetMapping("/")
    public ResponseEntity<ScoreBoard> viewScoreBoard(@RequestHeader long tournamentId, @RequestHeader long matchId, @RequestHeader long teamId) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewScoreBoard(new ScoreBoardModel(tournamentId, matchId, teamId))));
    }

    @PostMapping("/batsman-sb")
    public ResponseEntity<List<BatsmanSB>> viewBatsmanSB(@ModelAttribute ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewBatsmanSB(scoreBoardModel)));
    }

    @PostMapping("/bowler-sb")
    public ResponseEntity<List<BowlerSB>> viewBowlerSB(@ModelAttribute ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewBowlerSB(scoreBoardModel)));
    }

    @PostMapping("/extra-sb")
    public ResponseEntity<ExtraRuns> viewExtraRuns(@ModelAttribute ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewExtraRuns(scoreBoardModel)));
    }

    @PostMapping("/fall-of-wicket")
    public ResponseEntity<List<FallOfWicketSB>> viewFallOfWicketSB(@ModelAttribute ScoreBoardModel scoreBoardModel) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewFallOfWickets(scoreBoardModel)));
    }

}
