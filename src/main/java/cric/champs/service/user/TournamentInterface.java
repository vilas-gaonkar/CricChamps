package cric.champs.service.user;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.requestmodel.SetDateTimeModel;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Tournaments;
import cric.champs.resultmodels.TournamentResultModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TournamentInterface {

    TournamentResultModel registerTournament(Tournaments tournaments);

    List<Tournaments> getTournamentDetails(int pageSize, int pageNumber);

    Tournaments getTournament(long tournamentId);

    Tournaments getDetailsByTournamentCode(String tournamentCode);

    SuccessResultModel cancelTournament(long tournamentId);

    SuccessResultModel setTournamentDate(long tournamentId, LocalDate startDate, LocalDate endDate);

    SuccessResultModel setTournamentTime(long tournamentId, LocalTime startTime, LocalTime endTime) throws FixtureGenerationException;

    SuccessResultModel setTournamentDateTimes(SetDateTimeModel setDateTimeModel) throws FixtureGenerationException;

    SuccessResultModel setTournamentOver(long tournamentId, int numberOfOvers);

}
