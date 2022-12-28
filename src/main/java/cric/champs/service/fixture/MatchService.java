package cric.champs.service.fixture;

import cric.champs.entity.Matches;
import cric.champs.model.Versus;
import cric.champs.resultmodels.MatchCellResultModel;
import cric.champs.resultmodels.MatchResult;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchService implements MatchInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public List<Matches> viewAllMatches(long tournamentId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = 'false' order by " +
                        "matchDate",
                new BeanPropertyRowMapper<>(Matches.class), tournamentId);
    }

    @Override
    public List<Versus> viewMatchDetails(long tournamentId, long matchId) {
        return jdbcTemplate.query("select * from versus where matchId = ? ",
                new BeanPropertyRowMapper<>(Versus.class), matchId);
    }

    @Override
    public List<MatchResult> viewMatch(long tournamentId) {
        List<MatchResult> matchResults = new ArrayList<>();
        List<Matches> matches = viewAllMatches(tournamentId);
        for (Matches match : matches)
            matchResults.add(new MatchResult(match, viewMatchDetails(tournamentId, match.getMatchId())));
        return matchResults;
    }

    @Override
    public MatchResult matchInfo(long tournamentId, long matchId) {
        return new MatchResult(getMatchInfo(tournamentId, matchId), viewMatchDetails(tournamentId, matchId));
    }

    @Override
    public Matches getMatchInfo(long tournamentId, long matchId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from matches where tournamentId = ? and isCancelled = 'false' and matchId = ?",
                new BeanPropertyRowMapper<>(Matches.class), tournamentId, matchId).get(0);
    }

    @Override
    public List<MatchCellResultModel> info(long tournamentId) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        List<MatchCellResultModel> matchCellResultModel = new ArrayList<>();
        List<Matches> matches = viewAllMatches(tournamentId);
        for (Matches match : matches) {
            List<Versus> versus = viewMatchDetails(tournamentId, match.getMatchId());
            if (versus.isEmpty())
                matchCellResultModel.add(new MatchCellResultModel(match.getTournamentId(), match.getMatchId(),
                        match.getGroundId(), match.getMatchNumber(), match.getGroundName(), match.getMatchStatus(),
                        match.getMatchDate(), match.getMatchDay(), match.getMatchStartTime(), match.getMatchEndTime(),
                        null, null, 0, 0, 0,
                        0, null, null, null, 0,
                        0, 0, 0, null,
                        match.getCancelledReason()));
            else
                matchCellResultModel.add(new MatchCellResultModel(match.getTournamentId(), match.getMatchId(),
                        match.getGroundId(), match.getMatchNumber(), match.getGroundName(), match.getMatchStatus(), match.getMatchDate(),
                        match.getMatchDay(), match.getMatchStartTime(), match.getMatchEndTime(), versus.get(0).getTeamId(),
                        versus.get(0).getTeamName(), versus.get(0).getTotalScore(), versus.get(0).getTotalWickets(),
                        versus.get(0).getTotalOverPlayed(), versus.get(0).getTotalBallsPlayed(),
                        versus.get(0).getMatchResult(), versus.get(1).getTeamId(), versus.get(1).getTeamName(),
                        versus.get(1).getTotalScore(), versus.get(1).getTotalWickets(), versus.get(1).getTotalOverPlayed(),
                        versus.get(1).getTotalBallsPlayed(), versus.get(1).getMatchResult(), match.getCancelledReason()));
        }
        return matchCellResultModel;
    }

}
