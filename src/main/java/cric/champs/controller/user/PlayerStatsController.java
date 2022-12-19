package cric.champs.controller.user;

import cric.champs.entity.Teams;
import cric.champs.model.PlayerStats;
import cric.champs.service.scoreboardandlivescore.PlayerStatsInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/players-stats")
public class PlayerStatsController {

    @Autowired
    private PlayerStatsInterface playerStatsInterface;


    @GetMapping("/all-stats")
    ResponseEntity<List<PlayerStats>> viewAll(@RequestHeader String playerStats, @RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewPlayerStats(playerStats, tournamentId)));
    }

    @GetMapping("/most-runs")
    ResponseEntity<List<PlayerStats>> viewMostRuns(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostRuns(id)));
    }

    @GetMapping("/best-batting-average")
    ResponseEntity<List<PlayerStats>> viewBestBattingAverage(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestBattingAverage(id)));
    }

    @GetMapping("/best-bating-strike")
    ResponseEntity<List<PlayerStats>> viewBattingStrikeRate(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBattingStrikeRate(id)));
    }

    @GetMapping("/most-hundreds")
    ResponseEntity<List<PlayerStats>> viewMostHundreds(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostHundreds(id)));
    }

    @GetMapping("/most-fifties")
    ResponseEntity<List<PlayerStats>> viewMostFifties(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostFifties(id)));
    }

    @GetMapping("/most-fours")
    ResponseEntity<List<PlayerStats>> viewMostFours(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostFours(id)));
    }

    @GetMapping("/most-sixes")
    ResponseEntity<List<PlayerStats>> viewMostSixes(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostSixes(id)));
    }

    @GetMapping("/most-wicket")
    ResponseEntity<List<PlayerStats>> viewMostWicket(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostWicket(id)));
    }

    @GetMapping("/best-bowling-average")
    ResponseEntity<List<PlayerStats>> viewBestBowlingAverage(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestBowlingAverage(id)));
    }

    @GetMapping("/most-five-wicket")
    ResponseEntity<List<PlayerStats>> viewMostFiveWicket(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostFiveWicket(id)));
    }

    @GetMapping("/best-economy")
    ResponseEntity<List<PlayerStats>> viewBestEconomy(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestEconomy(id)));
    }

    @GetMapping("/best-bowling-strike-rate")
    ResponseEntity<List<PlayerStats>> viewBestBowlingStrikeRate(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestBowlingStrikeRate(id)));
    }

    //needed for both
    @GetMapping("/highest-score-team")
    ResponseEntity<List<Teams>> viewHighestScoreTeam(@RequestParam long id) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewHighestScoreTeam(id)));
    }

}
