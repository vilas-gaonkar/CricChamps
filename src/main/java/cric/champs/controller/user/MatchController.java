package cric.champs.controller.user;

import cric.champs.entity.Matches;
import cric.champs.model.Versus;
import cric.champs.resultmodels.MatchCellResultModel;
import cric.champs.resultmodels.MatchResult;
import cric.champs.service.fixture.MatchInterface;
import cric.champs.service.scoreboardandlivescore.LiveResultInterface;
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

    @GetMapping("/view")
    public ResponseEntity<List<Matches>> viewMatches(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(matchInterface.viewAllMatches(tournamentId)));
    }

    @GetMapping("/view-versus")
    public ResponseEntity<List<Versus>> viewVersus(@RequestHeader long tournamentId, @RequestHeader long matchId) {
        return ResponseEntity.of(Optional.of(matchInterface.viewMatchDetails(tournamentId, matchId)));
    }

    @GetMapping("/view-info")
    public ResponseEntity<MatchResult> viewMatchesInfo(@RequestHeader long tournamentId, @RequestHeader long matchId) {
        return ResponseEntity.of(Optional.of(matchInterface.matchInfo(tournamentId, matchId)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<List<MatchResult>> viewMatchInfo(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(matchInterface.viewMatch(tournamentId)));
    }

    @GetMapping("/info")
    public ResponseEntity<List<MatchCellResultModel>> viewInfo(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(matchInterface.info(tournamentId)));
    }

}
