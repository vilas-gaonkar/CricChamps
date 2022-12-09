package cric.champs.service.user;

import cric.champs.customexceptions.FixtureGenerationException;
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

    Tournaments getDetailsByTournamentCode(String tournamentCode);

    ResultModel cancelTournament(long tournamentId);

    ResultModel setTournamentDate(long tournamentId, LocalDate startDate, LocalDate endDate);

    ResultModel setTournamentTime(long tournamentId, LocalTime startTime, LocalTime endTime);

    ResultModel setTournamentDateTime(long tournamentId, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) throws FixtureGenerationException;

}
