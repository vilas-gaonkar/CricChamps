package cric.champs.controller.user;

import cric.champs.resultmodels.NameResult;
import cric.champs.service.names.GetNamesInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/name")
public class GetNamesController {

    @Autowired
    private GetNamesInterface getNamesInterface;

    @GetMapping("/teams")
    public ResponseEntity<List<NameResult>> getAllTeamNames(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(getNamesInterface.getAllTeamNames(tournamentId)));
    }

    @GetMapping("/grounds")
    public ResponseEntity<List<NameResult>> getAllGroundsNAme(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(getNamesInterface.getAllGroundsName(tournamentId)));
    }

    @GetMapping("/umpires")
    public ResponseEntity<List<NameResult>> getAllUmpiresNAme(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(getNamesInterface.getAllUmpiresName(tournamentId)));
    }

}
