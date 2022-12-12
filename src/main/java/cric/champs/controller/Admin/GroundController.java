package cric.champs.controller.Admin;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.entity.Grounds;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.user.GroundInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
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
    public ResponseEntity<SuccessResultModel> register(@ModelAttribute @Valid Grounds ground, @RequestPart @Nullable List<MultipartFile> groundPhoto) throws IOException {
        Map result;
        List<String> groundPhotos = new ArrayList<>();
        if (groundPhoto == null)
            groundPhotos = null;
        else if (groundPhoto.isEmpty())
            groundPhotos = null;
        else
            for (MultipartFile file : groundPhoto) {
                result = uploadImageTOCloud.uploadImage(file.getBytes(), ObjectUtils.asMap("resource type", "auto"));
                groundPhotos.add(result.get("url").toString());
            }
        return ResponseEntity.of(Optional.of(groundInterface.registerGrounds(ground, groundPhotos)));
    }

    @PutMapping("/edit")
    public ResponseEntity<SuccessResultModel> edit(@ModelAttribute @Valid Grounds ground, @RequestPart @Nullable List<MultipartFile> groundPhoto) throws IOException {
        Map result;
        List<String> groundPhotos = new ArrayList<>();
        if (groundPhoto == null)
            groundPhotos = null;
        else if (groundPhoto.isEmpty())
            groundPhotos = null;
        else
            for(MultipartFile file:groundPhoto) {
                result = uploadImageTOCloud.uploadImage(file.getBytes(), ObjectUtils.asMap("resource type", "auto"));
                groundPhotos.add(result.get("url").toString());
            }

        return ResponseEntity.of(Optional.of(groundInterface.editGround(ground,groundPhotos)));
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
    public ResponseEntity<SuccessResultModel> delete(@RequestParam long groundId, @RequestParam long tournamentId) {
        return ResponseEntity.of(Optional.of(groundInterface.deleteGrounds(groundId, tournamentId)));
    }

}
