package cric.champs.service.user;

import cric.champs.entity.Grounds;
import cric.champs.resultmodels.SuccessResultModel;

import java.util.List;

public interface GroundInterface {

    SuccessResultModel registerGrounds(Grounds grounds, List<String> groundPhoto);

    SuccessResultModel deleteGrounds(long groundId, long tournamentId);

    SuccessResultModel editGround(Grounds ground, List<String> groundPhoto);

    List<Grounds> getAllGrounds(long tournamentId, int pageSize, int pageNumber);

    Grounds getGround(long groundId, long tournamentId);

}
