package cric.champs.controller.Admin;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.livescorerequestmodels.SetDateTimeModel;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Tournaments;
import cric.champs.resultmodels.TournamentResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.TournamentInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tournament")
public class TournamentController {

    @Autowired
    private TournamentInterface tournamentInterface;

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @SuppressWarnings("rawtypes")
    @PostMapping("/create")
    public ResponseEntity<TournamentResultModel> register(@ModelAttribute Tournaments tournament, @RequestPart @Nullable MultipartFile logo) throws IOException {
        Map result = null;
        if (logo == null)
            tournament.setTournamentLogo(null);
        else if (logo.isEmpty())
            tournament.setTournamentLogo(null);
        else
            result = uploadImageTOCloud.uploadImage(logo.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        if (result != null)
            tournament.setTournamentLogo(result.get("url").toString());
        return ResponseEntity.of(Optional.of(tournamentInterface.registerTournament(tournament)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<List<Tournaments>> getAll(@RequestParam int pageSize, @RequestParam int pageNumber) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getTournamentDetails(pageSize, pageNumber)));
    }

    @GetMapping("/view")
    public ResponseEntity<Tournaments> get(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getTournament(tournamentId)));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<SuccessResultModel> cancelTournament(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(tournamentInterface.cancelTournament(tournamentId)));
    }

    @PatchMapping("/set-date")
    public ResponseEntity<SuccessResultModel> setTournamentDate(@RequestHeader long tournamentId,
                                                                @RequestHeader @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                @RequestHeader @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentDate(tournamentId, startDate, endDate)));
    }

    @PatchMapping("/set-time")
    public ResponseEntity<SuccessResultModel> setTournamentTime(@RequestHeader long tournamentId,
                                                                @RequestHeader @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                                                @RequestHeader @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) throws FixtureGenerationException {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentTime(tournamentId, startTime, endTime)));
    }

    @GetMapping("/get")
    public ResponseEntity<Tournaments> getTournament(@RequestHeader @Size(min = 6, max = 6, message = "Invalid tournament code") String tournamentCode) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getDetailsByTournamentCode(tournamentCode)));
    }

    @PatchMapping("/set-overs")
    public ResponseEntity<SuccessResultModel> setTournamentOver(@RequestHeader long tournamentId, @RequestHeader int numberOfOvers) {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentOver(tournamentId, numberOfOvers)));
    }

    @PatchMapping("/set-date-time")
    public ResponseEntity<SuccessResultModel> setTournamentDateTimes(@RequestBody SetDateTimeModel setDateTimeModel) throws FixtureGenerationException {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentDateTimes(setDateTimeModel)));
    }
}
