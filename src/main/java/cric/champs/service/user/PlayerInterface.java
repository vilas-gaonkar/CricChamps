package cric.champs.service.user;

import cric.champs.entity.Players;
import cric.champs.resultmodels.SuccessResultModel;

import java.util.List;

public interface PlayerInterface {

    SuccessResultModel registerPlayer(Players players);

    SuccessResultModel deletePlayer(long playerId, long teamId, long tournamentId);

    SuccessResultModel editPlayer(Players players);

    List<Players> getAllPlayers(long teamId, long tournamentId, int pageSize, int pageNumber);

    Players getPlayer(long playerId, long teamId, long tournamentId);
}
