package cric.champs.controller.Admin;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.model.LiveScoreModel;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.scoreboardandlivescore.LiveScoreUpdateInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/live/score")
public class LiveScoreUpdateController {

    @Autowired
    private LiveScoreUpdateInterface liveScoreUpdateInterface;

    @PostMapping("/update")
    ResponseEntity<LiveScoreUpdateModel> update(@RequestBody LiveScoreUpdateModel liveScoreUpdateModel) throws LiveScoreUpdationException {
        return ResponseEntity.of(Optional.of(liveScoreUpdateInterface.updateLiveScore(liveScoreUpdateModel)));
    }

    @PostMapping("/stop-match")
    public ResponseEntity<SuccessResultModel> stopModel(@ModelAttribute LiveScoreModel liveScoreModel, @RequestPart String reason) {
        return ResponseEntity.of(Optional.of(liveScoreUpdateInterface.stopMatch(liveScoreModel, reason)));
    }
}
