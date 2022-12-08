package cric.champs.service.user;

import cric.champs.entity.ResultModel;
import cric.champs.entity.Teams;

import java.util.List;

public interface TeamInterface {

    long registerTeam(Teams teams) throws Exception;

    ResultModel deleteTeam(long teamId, long tournamentId);

    ResultModel editTeam(Teams teams);

    List<Teams> getAllTeams(long tournamentId, int pageSize, int pageNumber);

    Teams getTeam(long teamId, long tournamentId);
}
