package cric.champs.service.user;

import cric.champs.entity.ResultModel;
import cric.champs.entity.Umpires;

import java.util.List;

public interface UmpiresInterface {

    ResultModel registerUmpires(Umpires umpires);

    ResultModel deleteUmpires(long umpireId, long tournamentId);

    ResultModel editUmpire(Umpires umpire);

    List<Umpires> getUmpireDetails(long tournamentId,int pageSize , int pageNumber);

    Umpires getUmpire(long umpireId , long tournamentId);

}
