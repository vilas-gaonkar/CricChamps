package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.Grounds;
import cric.champs.entity.ResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.GroundInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @PostMapping("/register")
    public ResponseEntity<ResultModel> register(@ModelAttribute Grounds ground, @RequestPart MultipartFile groundPhoto) throws IOException {
        Map result = null;
        if (groundPhoto == null)
            ground.setGroundPhoto(null);
        else if (groundPhoto.isEmpty())
            ground.setGroundPhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(groundPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            ground.setGroundPhoto(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(groundInterface.registerGrounds(ground, null)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ResultModel> edit(@ModelAttribute Grounds ground, @RequestPart MultipartFile groundPhoto) throws IOException {
        Map result = null;
        if (groundPhoto == null)
            ground.setGroundPhoto(null);
        else if (groundPhoto.isEmpty())
            ground.setGroundPhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(groundPhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            ground.setGroundPhoto(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(groundInterface.editGround(ground)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResultModel> delete(@RequestPart long groundId, @RequestPart long tournamentId){
        return ResponseEntity.of(Optional.of(groundInterface.deleteGrounds(groundId,tournamentId)));
    }

}
