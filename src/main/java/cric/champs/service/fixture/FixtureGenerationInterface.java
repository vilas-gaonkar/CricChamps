package cric.champs.service.fixture;

import cric.champs.entity.Tournaments;
import cric.champs.resultmodels.SuccessResultModel;

public interface FixtureGenerationInterface {
    SuccessResultModel generateFixture(long tournamentId) throws Exception;

    void roundRobinGenerationForKnockoutLeague(long tournamentId , String tournamentStage);

    boolean roundRobinGenerationForKnockoutNextMatches(Tournaments tournament);
}
