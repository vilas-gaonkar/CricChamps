package cric.champs.service.user;

import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Umpires;

import java.util.List;

public interface UmpiresInterface {

    SuccessResultModel registerUmpires(Umpires umpires);

    SuccessResultModel deleteUmpires(long umpireId, long tournamentId);

    SuccessResultModel editUmpire(Umpires umpire);

    List<Umpires> getUmpireDetails(long tournamentId, int pageSize, int pageNumber);

    Umpires getUmpire(long umpireId, long tournamentId);

}
