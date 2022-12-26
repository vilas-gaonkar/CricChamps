package cric.champs.rate.rattingandfaqs;

import cric.champs.resultmodels.SuccessResultModel;

public interface RatingsInterface {

    SuccessResultModel rating(int numberOfStarsRated, String feedback);

    double getRatting();
}
