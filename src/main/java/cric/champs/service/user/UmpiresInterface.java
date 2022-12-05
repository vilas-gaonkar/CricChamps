package cric.champs.service.user;

import cric.champs.entity.ResultModel;
import cric.champs.entity.Umpires;

public interface UmpiresInterface {

    ResultModel registerUmpires(Umpires umpires);

    ResultModel deleteUmpires(long umpireId, long tournamentId);

    ResultModel editUmpire(Umpires umpire);

}
