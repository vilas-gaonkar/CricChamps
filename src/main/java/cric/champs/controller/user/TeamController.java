package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Teams;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.TeamInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private TeamInterface teamInterface;

    @PostMapping("/register")
    public ResponseEntity<ResultModel> register(@ModelAttribute @Valid Teams team, @RequestPart MultipartFile teamPhoto) throws IOException {
        Map result = null;
        if (teamPhoto == null)
            team.setTeamLogo(null);
        else if (teamPhoto.isEmpty())
            team.setTeamLogo(null);
        else
            result = uploadImageTOCloud.uploadImage(teamPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            team.setTeamLogo(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(teamInterface.registerTeam(team)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ResultModel> edit(@ModelAttribute @Valid Teams team, @RequestPart MultipartFile teamPhoto) throws IOException {
        Map result = null;
        if (teamPhoto == null)
            team.setTeamLogo(null);
        else if (teamPhoto.isEmpty())
            team.setTeamLogo(null);
        else
            result = uploadImageTOCloud.uploadImage(teamPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            team.setTeamLogo(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(teamInterface.editTeam(team)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResultModel> delete(@RequestPart long teamId, @RequestPart long tournamentId) {
        return ResponseEntity.of(Optional.of(teamInterface.deleteTeam(teamId, tournamentId)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<?> getAllTeams(@RequestParam long tournamentId, @RequestParam int pageSize, @RequestParam int pageNumber) {

        return ResponseEntity.of(Optional.of(teamInterface.getAllTeams(tournamentId, pageSize, pageNumber)));
    }

    @GetMapping("/view")
    public ResponseEntity<?> getTeam(@RequestParam long teamId, @RequestParam long tournamentId) {

        return ResponseEntity.of(Optional.of(teamInterface.getTeam(teamId, tournamentId)));
    }


}
