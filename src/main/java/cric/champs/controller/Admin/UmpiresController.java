package cric.champs.controller.Admin;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Umpires;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.UmpiresInterface;
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

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("/umpire")
public class UmpiresController {

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private UmpiresInterface umpiresInterface;

    @PostMapping("/add")
    public ResponseEntity<SuccessResultModel> register(@ModelAttribute @Valid Umpires umpire, @Nullable @RequestPart MultipartFile profilePhoto) throws IOException {
        Map result = null;
        if (profilePhoto == null)
            umpire.setUmpirePhoto(null);
        else if (profilePhoto.isEmpty())
            umpire.setUmpirePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(profilePhoto.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        if (result != null)
            umpire.setUmpirePhoto(result.get("url").toString());
        return ResponseEntity.of(Optional.of(umpiresInterface.registerUmpires(umpire)));
    }

    @PutMapping("/edit")
    public ResponseEntity<SuccessResultModel> edit(@ModelAttribute @Valid Umpires umpire, @Nullable @RequestPart MultipartFile profilePhoto) throws IOException {
        Map result = null;
        if (profilePhoto == null)
            umpire.setUmpirePhoto(null);
        else if (profilePhoto.isEmpty())
            umpire.setUmpirePhoto(null);
        else
            result = uploadImageTOCloud.uploadImage(profilePhoto.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        if (result != null)
            umpire.setUmpirePhoto(result.get("url").toString());
        return ResponseEntity.of(Optional.of(umpiresInterface.editUmpire(umpire)));
    }

    @GetMapping("/view-all")
    public ResponseEntity<List<Umpires>> view(@RequestHeader long tournamentId, @RequestParam int pageSize, @RequestParam int pageNumber) {
        return ResponseEntity.of(Optional.of(umpiresInterface.getUmpireDetails(tournamentId, pageSize, pageNumber)));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<SuccessResultModel> delete(@RequestPart long umpireId, @RequestPart long tournamentId) {
        return ResponseEntity.of(Optional.of(umpiresInterface.deleteUmpires(umpireId, tournamentId)));
    }

    @GetMapping("/view")
    public ResponseEntity<Umpires> getUmpire(@RequestHeader long umpireId, @RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(umpiresInterface.getUmpire(umpireId, tournamentId)));
    }
}
