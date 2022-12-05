package cric.champs.service.user;

import cric.champs.entity.Players;
import cric.champs.entity.ResultModel;

import java.util.List;

public interface PlayerInterface {

    ResultModel registerPlayer(Players players);

    ResultModel deletePlayer(long playerId, long teamId, long tournamentId);

    ResultModel editPlayer(Players players);

    List<Players> getAllPlayers(long teamId, long tournamentId, int pageSize, int pageNumber);

    Players getPlayer(long playerId, long teamId, long tournamentId);
}
