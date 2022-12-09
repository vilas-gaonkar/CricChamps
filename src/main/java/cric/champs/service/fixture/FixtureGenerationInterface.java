package cric.champs.service.fixture;

import cric.champs.resultmodels.SuccessResultModel;

public interface FixtureGenerationInterface {
    SuccessResultModel generateFixture(long tournamentId) throws Exception;
}
