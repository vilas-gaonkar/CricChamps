package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.customexceptions.SignupException;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Users;
import cric.champs.service.fixture.FixtureGenerationInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/fixture")
public class FixtureController {
    @Autowired
    FixtureGenerationInterface fixtureGenerationInterface;

    @PostMapping("/generate")
    public ResponseEntity<ResultModel> generateFixture(@RequestHeader long tournamentId) throws Exception {
        return ResponseEntity.of(Optional.of(fixtureGenerationInterface.generateFixture(tournamentId)));
    }



}
