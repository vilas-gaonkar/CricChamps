package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.Grounds;
import cric.champs.entity.ResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.GroundInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/ground")
public class GroundController {

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private GroundInterface groundInterface;

    @SuppressWarnings("rawtypes")
    @PostMapping("/add")
    public ResponseEntity<ResultModel> register(@ModelAttribute @Valid Grounds ground, @RequestPart @Nullable MultipartFile groundPhoto) throws IOException {
        Map result = null;
        if (groundPhoto == null)
            ground.setGroundPhoto(null);
        else if (groundPhoto.isEmpty())
            ground.setGroundPhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(groundPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            ground.setGroundPhoto(result.get("url").toString());
        return ResponseEntity.of(Optional.of(groundInterface.registerGrounds(ground, null)));
    }

    @PutMapping("/edit")
    public ResponseEntity<ResultModel> edit(@ModelAttribute @Valid Grounds ground, @RequestPart @Nullable MultipartFile groundPhoto) throws IOException {
        Map result = null;
        if (groundPhoto == null)
            ground.setGroundPhoto(null);
        else if (groundPhoto.isEmpty())
            ground.setGroundPhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(groundPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            ground.setGroundPhoto(result.get("url").toString());
        return ResponseEntity.of(Optional.of(groundInterface.editGround(ground)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<List<Grounds>> view(@RequestHeader long tournamentId, @RequestParam int pageSize, @RequestParam int pageNumber) {
        return ResponseEntity.of(Optional.of(groundInterface.getAllGrounds(tournamentId, pageSize, pageNumber)));
    }

    @GetMapping("/view")
    public ResponseEntity<Grounds> viewGround(@RequestHeader long groundId, @RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(groundInterface.getGround(groundId, tournamentId)));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResultModel> delete(@RequestParam long groundId, @RequestParam long tournamentId) {
        return ResponseEntity.of(Optional.of(groundInterface.deleteGrounds(groundId, tournamentId)));
    }

}
