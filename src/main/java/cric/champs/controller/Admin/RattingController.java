package cric.champs.controller.Admin;

import cric.champs.rate.model.HelpAndFAQs;
import cric.champs.rate.rattingandfaqs.HelpAndFAQsInterface;
import cric.champs.rate.rattingandfaqs.RatingsInterface;
import cric.champs.resultmodels.SuccessResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ratting")
public class RattingController {

    @Autowired
    private RatingsInterface ratingsInterface;

    @Autowired
    private HelpAndFAQsInterface helpAndFAQsInterface;

    @PostMapping("/")
    public ResponseEntity<SuccessResultModel> rateApp(@RequestParam int numberOfStarRated, @RequestPart @Nullable String feedback) {
        return ResponseEntity.of(Optional.of(ratingsInterface.rating(numberOfStarRated, feedback)));
    }

    @GetMapping("/help-faqs")
    public ResponseEntity<List<HelpAndFAQs>> helpAndFAQs() {
        return ResponseEntity.of(Optional.of(helpAndFAQsInterface.helpAndFAQs()));
    }

}
