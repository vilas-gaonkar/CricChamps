package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Umpires;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.UmpiresInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("/umpire")
public class UmpiresController {

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private UmpiresInterface umpiresInterface;

    @PostMapping("/register")
    public ResponseEntity<ResultModel> register(@ModelAttribute Umpires umpire, @Nullable @RequestPart MultipartFile profilePhoto) throws IOException {
        Map result = null;
        if (profilePhoto == null)
            umpire.setUmpirePhoto(null);
        else if (profilePhoto.isEmpty())
            umpire.setUmpirePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(profilePhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            umpire.setUmpirePhoto(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(umpiresInterface.registerUmpires(umpire)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ResultModel> edit(@ModelAttribute Umpires umpire, @Nullable @RequestPart MultipartFile profilePhoto) throws IOException {
        Map result = null;
        if (profilePhoto == null)
            umpire.setUmpirePhoto(null);
        else if (profilePhoto.isEmpty())
            umpire.setUmpirePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(profilePhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            umpire.setUmpirePhoto(result.get("url").toString());

        try {
            return ResponseEntity.of(Optional.of(umpiresInterface.editUmpire(umpire)));
        } catch (Exception exception) {
            return ResponseEntity.ok(new ResultModel(exception.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResultModel> delete(@RequestPart long umpireId,@RequestPart long tournamentId){
        return ResponseEntity.of(Optional.of(umpiresInterface.deleteUmpires(umpireId,tournamentId)));
    }

}