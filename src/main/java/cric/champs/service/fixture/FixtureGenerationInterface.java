package cric.champs.service.fixture;

import cric.champs.entity.ResultModel;

public interface FixtureGenerationInterface {
    ResultModel generateFixture(long tournamentId);
}
