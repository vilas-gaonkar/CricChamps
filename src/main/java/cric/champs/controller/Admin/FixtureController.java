package cric.champs.controller.Admin;

import cric.champs.entity.Tournaments;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.fixture.FixtureGenerationInterface;
import cric.champs.service.fixture.FixtureService;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/fixture")
public class FixtureController {
    @Autowired
    FixtureGenerationInterface fixtureGenerationInterface;

    @Autowired
    FixtureService fixtureService;

    @Autowired
    SystemInterface systemInterface;

    @PostMapping("/generate")
    public ResponseEntity<SuccessResultModel> generateFixture(@RequestHeader long tournamentId) throws Exception {
        return ResponseEntity.of(Optional.of(fixtureGenerationInterface.generateFixture(tournamentId)));
    }

    @PostMapping("/final")
    public ResponseEntity<Boolean> generateFix(@RequestHeader long tournamentId) {
        Tournaments tournaments = systemInterface.verifyTournamentId(tournamentId).get(0);
        return ResponseEntity.of(Optional.of(fixtureService.roundRobinGenerationForKnockoutNextMatches(tournaments)));
    }

}
