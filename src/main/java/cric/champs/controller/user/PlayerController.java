package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.Players;
import cric.champs.entity.ResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.PlayerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ResultModel> register(@ModelAttribute Players player, @RequestPart MultipartFile playerPhoto) throws IOException {
        Map result = null;
        if (playerPhoto == null)
            player.setProfilePhoto(null);
        else if (playerPhoto.isEmpty())
            player.setProfilePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(playerPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            player.setProfilePhoto(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(playerInterface.registerPlayer(player)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/edit")
    public ResponseEntity<ResultModel> edit(@ModelAttribute Players player, @RequestPart MultipartFile playerPhoto) throws IOException {
        Map result = null;
        if (playerPhoto == null)
            player.setProfilePhoto(null);
        else if (playerPhoto.isEmpty())
            player.setProfilePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(playerPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            player.setProfilePhoto(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(playerInterface.editPlayer(player)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResultModel> delete(@RequestPart long playerId, @RequestPart long teamId, @RequestPart long tournamentId) {
        return ResponseEntity.of(Optional.of(playerInterface.deletePlayer(playerId, teamId, tournamentId)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<?> getAllPlayers(@RequestPart long teamId, @RequestPart long tournamentId, @RequestParam int pageSize, @RequestParam int pageNumber) {

        return ResponseEntity.of(Optional.of(playerInterface.getAllPlayers(teamId, tournamentId, pageSize, pageNumber)));
    }

    @GetMapping("/view")
    public ResponseEntity<?> getPlayer(@RequestPart long playerId, @RequestPart long teamId, @RequestPart long tournamentId) {

        return ResponseEntity.of(Optional.of(playerInterface.getPlayer(playerId, teamId, tournamentId)));
    }
}
