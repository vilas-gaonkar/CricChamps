package cric.champs.service.fixture;

import cric.champs.entity.Tournaments;
import cric.champs.resultmodels.SuccessResultModel;

public interface FixtureGenerationInterface {
    SuccessResultModel generateFixture(long tournamentId) throws Exception;

    boolean roundRobinGenerationForKnockoutLeague(Tournaments tournament);

    boolean roundRobinGenerationForKnockoutNextMatches(Tournaments tournament);
}
