package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Tournaments;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.TournamentInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
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
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@ModelAttribute Tournaments tournament, @RequestPart @Nullable MultipartFile logo) throws IOException {
        Map result = null;
        if (logo == null)
            tournament.setTournamentLogo(null);
        else if (logo.isEmpty())
            tournament.setTournamentLogo(null);
        else
            result = uploadImageTOCloud.uploadImage(logo.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            tournament.setTournamentLogo(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(tournamentInterface.registerTournament(tournament)));
        } catch (Exception exception) {
            return ResponseEntity.ok(Collections.singletonMap("error message", exception.getMessage()));
        }
    }

    @GetMapping("/view-all")
    public ResponseEntity<List<Tournaments>> getAll(@RequestParam int pageSize, @RequestParam int pageNumber) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getTournamentDetails(pageSize, pageNumber)));
    }

    @GetMapping("/view")
    public ResponseEntity<Tournaments> get(long tournamentId) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getTournament(tournamentId)));
    }

    @PutMapping("/cancel")
    public ResponseEntity<ResultModel> cancelTournament(@RequestPart long tournamentId) {
        return ResponseEntity.of(Optional.of(tournamentInterface.cancelTournament(tournamentId)));
    }

    @PatchMapping("/set-date")
    public ResponseEntity<ResultModel> setTournamentDate(@RequestPart long tournamentId,
                                                         @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentDate(tournamentId, startDate, endDate)));
    }

    @PatchMapping("/set-time")
    public ResponseEntity<ResultModel> setTournamentTime(@RequestPart long tournamentId,
                                                         @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                                         @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentTime(tournamentId, startTime, endTime)));
    }

    @PatchMapping("/set-date-time")
    public ResponseEntity<ResultModel> setTournamentDateTime(@RequestPart long tournamentId,
                                                             @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                             @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                             @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                                             @RequestPart @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        return ResponseEntity.of(Optional.of(tournamentInterface.setTournamentDateTime(tournamentId, startDate, endDate, startTime, endTime)));
    }

}
