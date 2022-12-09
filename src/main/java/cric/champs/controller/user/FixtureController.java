package cric.champs.controller.user;

import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.fixture.FixtureGenerationInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/fixture")
public class FixtureController {
    @Autowired
    FixtureGenerationInterface fixtureGenerationInterface;

    @PostMapping("/generate")
    public ResponseEntity<SuccessResultModel> generateFixture(@RequestHeader long tournamentId) throws Exception {
        return ResponseEntity.of(Optional.of(fixtureGenerationInterface.generateFixture(tournamentId)));
    }



}
