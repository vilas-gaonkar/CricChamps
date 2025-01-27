package cric.champs.controller.Admin;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.Players;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.PlayerInterface;
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
@RequestMapping("/player")
public class PlayerController {
    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private PlayerInterface playerInterface;

    @SuppressWarnings("rawtypes")
    @PostMapping("/register")
    public ResponseEntity<SuccessResultModel> register(@ModelAttribute @Valid Players player, @RequestPart @Nullable MultipartFile playerPhoto) throws IOException {
        Map result = null;
        if (playerPhoto == null)
            player.setProfilePhoto(null);
        else if (playerPhoto.isEmpty())
            player.setProfilePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(playerPhoto.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        if (result != null)
            player.setProfilePhoto(result.get("url").toString());
        return ResponseEntity.of(Optional.of(playerInterface.registerPlayer(player)));
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/edit")
    public ResponseEntity<SuccessResultModel> edit(@ModelAttribute @Valid Players player, @RequestPart @Nullable MultipartFile playerPhoto) throws IOException {
        Map result = null;
        if (playerPhoto == null)
            player.setProfilePhoto(null);
        else if (playerPhoto.isEmpty())
            player.setProfilePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(playerPhoto.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        if (result != null)
            player.setProfilePhoto(result.get("url").toString());
        return ResponseEntity.of(Optional.of(playerInterface.editPlayer(player)));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<SuccessResultModel> delete(@RequestHeader long playerId, @RequestHeader long teamId, @RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerInterface.deletePlayer(playerId, teamId, tournamentId)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<?> getAllPlayers(@RequestHeader long teamId, @RequestHeader long tournamentId) {

        return ResponseEntity.of(Optional.of(playerInterface.getAllPlayers(teamId, tournamentId)));
    }

    @GetMapping("/view")
    public ResponseEntity<?> getPlayer(@RequestHeader long playerId, @RequestHeader long teamId, @RequestHeader long tournamentId) {

        return ResponseEntity.of(Optional.of(playerInterface.getPlayer(playerId, teamId, tournamentId)));
    }

}
