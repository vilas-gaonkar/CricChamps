package cric.champs.service.user;

import cric.champs.customexceptions.InvalidFieldException;
import cric.champs.entity.Players;
import cric.champs.resultmodels.SuccessResultModel;

import java.util.List;

public interface PlayerInterface {

    SuccessResultModel registerPlayer(Players players) throws InvalidFieldException;

    SuccessResultModel deletePlayer(long playerId, long teamId, long tournamentId);

    SuccessResultModel editPlayer(Players players);

    List<Players> getAllPlayers(long teamId, long tournamentId);

    Players getPlayer(long playerId, long teamId, long tournamentId);
}
