package cric.champs.controller.Admin;

import cric.champs.model.LiveScoreModel;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.scoreboardandlivescore.LiveResultInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/match")
public class MatchController {
    @Autowired
    LiveResultInterface liveResultInterface;

    @PostMapping("/stop-match")
    public ResponseEntity<SuccessResultModel> stopModel(@ModelAttribute LiveScoreModel liveScoreModel , @RequestPart String reason ){
        return ResponseEntity.of(Optional.of(liveResultInterface.stopMatch(liveScoreModel,reason)));
    }
}
