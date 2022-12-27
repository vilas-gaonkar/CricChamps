package cric.champs.service.fixture;

import cric.champs.entity.Matches;
import cric.champs.model.Versus;
import cric.champs.resultmodels.MatchCellResultModel;
import cric.champs.resultmodels.MatchResult;

import java.util.List;

public interface MatchInterface {
    List<Matches> viewAllMatches(long tournamentId);

    List<Versus> viewMatchDetails(long tournamentId, long matchId);

    List<MatchResult> viewMatch(long tournamentId);

    MatchResult matchInfo(long tournamentId , long matchId);

    Matches getMatchInfo(long tournamentId , long matchId);

    List<MatchCellResultModel> info(long tournamentId);
}
