package cric.champs.service.fixture;

import cric.champs.entity.Matches;
import cric.champs.model.Versus;

import java.util.List;

public interface MatchInterface {
    List<Matches> viewAllMatches(long tournamentId);

    List<Versus> viewMatchDetails(long tournamentId, long matchId);

}
