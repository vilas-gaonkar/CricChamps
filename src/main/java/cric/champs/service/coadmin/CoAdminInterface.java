package cric.champs.service.coadmin;

import cric.champs.model.CoAdmin;
import cric.champs.requestmodel.CoAdminRequestModel;
import cric.champs.resultmodels.CoAdminResult;
import cric.champs.resultmodels.SuccessResultModel;

import java.util.List;

public interface CoAdminInterface {

    List<CoAdminResult> viewCoAdmin(long tournamentId);

    SuccessResultModel assignCoAdminToMatch(CoAdminRequestModel coAdminRequestModel);
}
