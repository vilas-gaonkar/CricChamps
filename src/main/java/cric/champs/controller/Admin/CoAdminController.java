package cric.champs.controller.Admin;

import cric.champs.requestmodel.CoAdminRequestModel;
import cric.champs.resultmodels.CoAdminResult;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.coadmin.CoAdminInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/co-admin")
public class CoAdminController {

    @Autowired
    private CoAdminInterface coAdminInterface;

    @PostMapping("/assign")
    public ResponseEntity<SuccessResultModel> assignCoAdmin(@ModelAttribute CoAdminRequestModel coAdminRequestModel){
        return ResponseEntity.of(Optional.of(coAdminInterface.assignCoAdminToMatch(coAdminRequestModel)));
    }

    @GetMapping("/view")
    public ResponseEntity<List<CoAdminResult>> viewCoAdmin(@RequestHeader long tournamentId){
        return ResponseEntity.of(Optional.of(coAdminInterface.viewCoAdmin(tournamentId)));
    }

}
