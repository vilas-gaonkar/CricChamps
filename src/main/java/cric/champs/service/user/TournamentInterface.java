package cric.champs.service.user;

import cric.champs.entity.ResultModel;
import cric.champs.entity.Tournaments;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface TournamentInterface {

    Map<String, String> registerTournament(Tournaments tournaments);

    List<Tournaments> getTournamentDetails(int pageSize, int pageNumber);

    Tournaments getTournament(long tournamentId);

    ResultModel cancelTournament(long tournamentId);

    ResultModel setTournamentDate(LocalDate startDate , LocalDate endDate);

    ResultModel setTournamentTime(LocalTime startTime , LocalTime endTime);

}
