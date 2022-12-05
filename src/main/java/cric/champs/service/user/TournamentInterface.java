package cric.champs.service.user;

import cric.champs.entity.ResultModel;
import cric.champs.entity.Tournaments;

import java.util.Map;

public interface TournamentInterface {

    Map<String, String> registerTournament(Tournaments tournaments);

    ResultModel cancelTournament(long tournamentId);

}
