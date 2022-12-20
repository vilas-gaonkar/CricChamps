package cric.champs.controller.Admin;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.Teams;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.resultmodels.TeamResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.TeamInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private TeamInterface teamInterface;

    @PostMapping("/create")
    public HttpEntity<TeamResultModel> register(@ModelAttribute @Valid Teams team, @RequestPart @Nullable MultipartFile teamPhoto) throws Exception {
        Map result = null;
        if (teamPhoto == null)
            team.setTeamLogo(null);
        else if (teamPhoto.isEmpty())
            team.setTeamLogo(null);
        else
            result = uploadImageTOCloud.uploadImage(teamPhoto.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        if (result != null)
            team.setTeamLogo(result.get("url").toString());
        return new HttpEntity<>(teamInterface.registerTeam(team));
    }

    @PutMapping("/edit")
    public ResponseEntity<SuccessResultModel> edit(@ModelAttribute @Valid Teams team, @RequestPart @Nullable MultipartFile teamPhoto) throws IOException {
        Map result = null;
        if (teamPhoto == null)
            team.setTeamLogo(null);
        else if (teamPhoto.isEmpty())
            team.setTeamLogo(null);
        else
            result = uploadImageTOCloud.uploadImage(teamPhoto.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
        if (result != null)
            team.setTeamLogo(result.get("url").toString());
        return ResponseEntity.of(Optional.of(teamInterface.editTeam(team)));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<SuccessResultModel> delete(@RequestHeader long teamId, @RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(teamInterface.deleteTeam(teamId, tournamentId)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<?> getAllTeams(@RequestHeader long tournamentId) {

        return ResponseEntity.of(Optional.of(teamInterface.getAllTeams(tournamentId)));
    }

    @GetMapping("/view")
    public ResponseEntity<?> getTeam(@RequestHeader long teamId, @RequestHeader long tournamentId) {

        return ResponseEntity.of(Optional.of(teamInterface.getTeam(teamId, tournamentId)));
    }

}