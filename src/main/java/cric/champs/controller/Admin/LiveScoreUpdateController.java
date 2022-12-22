package cric.champs.controller.Admin;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.resultmodels.PlayersResult;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.scoreboardandlivescore.LiveScoreUpdateInterface;
import cric.champs.service.wicketPopUp.PopUpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/live/score")
public class LiveScoreUpdateController {

    @Autowired
    private LiveScoreUpdateInterface liveScoreUpdateInterface;

    @Autowired
    private PopUpInterface popUpInterface;

    @PostMapping("/update")
    ResponseEntity<LiveScoreUpdateModel> update(@RequestBody LiveScoreUpdateModel liveScoreUpdateModel) throws LiveScoreUpdationException, FixtureGenerationException {
        return ResponseEntity.of(Optional.of(liveScoreUpdateInterface.updateLiveScore(liveScoreUpdateModel)));
    }

    @DeleteMapping("/stop-match")
    public ResponseEntity<SuccessResultModel> stopModel(@RequestHeader long matchId, @RequestHeader long tournamentId, @RequestHeader String reason) {
        return ResponseEntity.of(Optional.of(liveScoreUpdateInterface.stopMatch(matchId,tournamentId, reason)));
    }

    @GetMapping("/remaining-batsman")
    public ResponseEntity<List<PlayersResult>> remainingBatsman(@RequestHeader long tournamentId, @RequestHeader long matchId, @RequestHeader long teamId){
        return ResponseEntity.of(Optional.of(popUpInterface.remainingBatsman(tournamentId,matchId,teamId)));
    }
    @GetMapping("/remaining-bowlers")
    public ResponseEntity<List<PlayersResult>> remainingBowlers(@RequestHeader long tournamentId, @RequestHeader long matchId, @RequestHeader long teamId){
        return ResponseEntity.of(Optional.of(popUpInterface.remainingBowlers(tournamentId,matchId,teamId)));
    }

    @GetMapping("/fielders")
    public ResponseEntity<List<PlayersResult>> fielders(@RequestHeader long tournamentId, @RequestHeader long matchId, @RequestHeader long teamId){
        return ResponseEntity.of(Optional.of(popUpInterface.fielders(tournamentId,matchId,teamId)));
    }

}
