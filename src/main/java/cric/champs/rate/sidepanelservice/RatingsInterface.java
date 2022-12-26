package cric.champs.rate.sidepanelservice;

import cric.champs.resultmodels.SuccessResultModel;

public interface RatingsInterface {

    SuccessResultModel rating(int numberOfStarsRated, String feedback);

}
