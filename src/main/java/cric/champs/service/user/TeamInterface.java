package cric.champs.service.user;

import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Teams;
import cric.champs.resultmodels.TeamResultModel;

import java.util.List;

public interface TeamInterface {

    TeamResultModel registerTeam(Teams teams) throws Exception;

    SuccessResultModel deleteTeam(long teamId, long tournamentId);

    SuccessResultModel editTeam(Teams teams);

    List<Teams> getAllTeams(long tournamentId, int pageSize, int pageNumber);

    Teams getTeam(long teamId, long tournamentId);
}
