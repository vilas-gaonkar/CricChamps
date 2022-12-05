package cric.champs.service.user;

import cric.champs.entity.Grounds;
import cric.champs.entity.ResultModel;

import java.util.List;

public interface GroundInterface {

    ResultModel registerGrounds(Grounds grounds , List<String> groundPhoto);

    ResultModel deleteGrounds(long groundId,long tournamentId);

    ResultModel editGround(Grounds ground);

}
