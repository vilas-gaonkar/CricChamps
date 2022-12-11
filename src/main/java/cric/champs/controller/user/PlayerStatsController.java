package cric.champs.controller.user;

import cric.champs.model.PlayerStats;
import cric.champs.service.scoreboardandlivescore.PlayerStatsInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/players-stats")
public class PlayerStatsController {

    @Autowired
    private PlayerStatsInterface playerStatsInterface;

    @GetMapping("/most-runs")
    ResponseEntity<List<PlayerStats>> viewMostRuns(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostRuns(tournamentId)));
    }

    @GetMapping("/best-batting-average")
    ResponseEntity<List<PlayerStats>> viewBestBattingAverage(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestBattingAverage(tournamentId)));
    }

    @GetMapping("/best-bating-strike")
    ResponseEntity<List<PlayerStats>> viewBattingStrikeRate(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBattingStrikeRate(tournamentId)));
    }

    @GetMapping("/most-hundreds")
    ResponseEntity<List<PlayerStats>> viewMostHundreds(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostHundreds(tournamentId)));
    }

    @GetMapping("/most-fifties")
    ResponseEntity<List<PlayerStats>> viewMostFifties(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostFifties(tournamentId)));
    }

    @GetMapping("/most-fours")
    ResponseEntity<List<PlayerStats>> viewMostFours(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostFours(tournamentId)));
    }

    @GetMapping("/most-sixes")
    ResponseEntity<List<PlayerStats>> viewMostSixes(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostSixes(tournamentId)));
    }

    @GetMapping("/most-wicket")
    ResponseEntity<List<PlayerStats>> viewMostWicket(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostWicket(tournamentId)));
    }

    @GetMapping("/best-bowling-average")
    ResponseEntity<List<PlayerStats>> viewBestBowlingAverage(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestBowlingAverage(tournamentId)));
    }

    @GetMapping("/most-five-wicket")
    ResponseEntity<List<PlayerStats>> viewMostFiveWicket(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewMostFiveWicket(tournamentId)));
    }

    @GetMapping("/best-economy")
    ResponseEntity<List<PlayerStats>> viewBestEconomy(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestEconomy(tournamentId)));
    }

    @GetMapping("/best-bowling-strike-rate")
    ResponseEntity<List<PlayerStats>> viewBestBowlingStrikeRate(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewBestBowlingStrikeRate(tournamentId)));
    }

    @GetMapping("/highest-score-team")
    ResponseEntity<List<PlayerStats>> viewHighestScoreTeam(@RequestHeader long tournamentId) {
        return ResponseEntity.of(Optional.of(playerStatsInterface.viewHighestScoreTeam(tournamentId)));
    }

}
