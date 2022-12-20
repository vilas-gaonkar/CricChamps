package cric.champs.service.wicketPopUp;

import cric.champs.resultmodels.PlayersResult;

import java.util.List;

public interface PopUpInterface {

    List<PlayersResult> remainingBatsman(long tournamentId,long matchId ,long teamId);

    List<PlayersResult> remainingBowlers(long tournamentId,long matchId ,long teamId);

    List<PlayersResult> fielders(long tournamentId, long matchId , long teamId);

}
