package cric.champs.controller.Admin;

import cric.champs.entity.Matches;
import cric.champs.model.LiveScoreModel;
import cric.champs.model.Versus;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.fixture.MatchInterface;
import cric.champs.service.scoreboardandlivescore.LiveResultInterface;
import cric.champs.service.scoreboardandlivescore.ScoreboardInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    private LiveResultInterface liveResultInterface;

    @Autowired
    private MatchInterface matchInterface;

    @Autowired
    private ScoreboardInterface scoreboardInterface;

    @GetMapping("/view")
    public ResponseEntity<List<Matches>> viewMatches(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(matchInterface.viewAllMatches(tournamentId)));
    }

    @GetMapping("/view-versus")
    public ResponseEntity<List<Versus>> viewVersus(@RequestHeader long tournamentId, @RequestHeader long matchId) {
        return ResponseEntity.of(Optional.of(scoreboardInterface.viewMatchDetails(tournamentId, matchId)));
    }

}
