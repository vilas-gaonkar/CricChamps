package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Tournaments;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.TournamentInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<Map<String, String>> register(@ModelAttribute Tournaments tournament, @RequestPart MultipartFile logo) throws IOException {
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

    @GetMapping("/view")
    public ResponseEntity<List<Tournaments>> getAll(@RequestParam int pageSize, @RequestParam int pageNumber) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getTournamentDetails(pageSize, pageNumber)));
    }

    @GetMapping("/view/tournament")
    public ResponseEntity<Tournaments> get(long tournamentId) {
        return ResponseEntity.of(Optional.of(tournamentInterface.getTournament(tournamentId)));
    }

    @PutMapping("/edit")
    public ResponseEntity<ResultModel> edit(@RequestPart long tournamentId) {
        return ResponseEntity.of(Optional.of(tournamentInterface.cancelTournament(tournamentId)));
    }

}
