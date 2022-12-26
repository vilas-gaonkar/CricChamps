package cric.champs.setting.sidepanel;

import cric.champs.setting.sidepanelservice.HelpAndFAQsInterface;
import cric.champs.setting.sidepanelservice.RatingsInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/side-panel")
public class SidePanelController {

    @Autowired
    private RatingsInterface ratingsInterface;

    @Autowired
    private HelpAndFAQsInterface helpAndFAQsInterface;

    @PatchMapping("/rate-app")
    public ResponseEntity<String> rateApp(@RequestParam @Nullable int numberOfStarRated, @RequestPart @Nullable String feedback) {
        return ResponseEntity.of(Optional.of(ratingsInterface.rating(numberOfStarRated, feedback)));
    }

    @PostMapping("/help-and-faqs")
    public ResponseEntity<?> helpAndFAQs(){
        return ResponseEntity.of(Optional.of(helpAndFAQsInterface.helpAndFAQs()));
    }

}
