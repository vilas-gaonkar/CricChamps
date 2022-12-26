package cric.champs.service.names;

import cric.champs.resultmodels.NameResult;

import java.util.List;

public interface GetNamesInterface {

    List<NameResult> getAllTeamNames(long tournamentId);

    List<NameResult> getAllGroundsName(long tournamentId);

    List<NameResult> getAllUmpiresName(long tournamentId);

}
