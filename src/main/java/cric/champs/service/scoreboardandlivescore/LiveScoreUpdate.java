package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.FixtureGenerationException;
import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.entity.*;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.model.*;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.*;
import cric.champs.service.fixture.FixtureGenerationInterface;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class LiveScoreUpdate implements LiveScoreUpdateInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    private Integer numberOfOversOfTournament;

    @Autowired
    private FixtureGenerationInterface fixtureGenerationInterface;

    @Override
    public LiveScoreUpdateModel updateLiveScore(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException, FixtureGenerationException {
        checkValidationBeforeUpdate(liveScoreModel);
        numberOfOversOfTournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).get(0).getNumberOfOvers();

        doInitialConditions(liveScoreModel);

        if (!liveScoreModel.getExtraModel().isExtraStatus() && liveScoreModel.getBall() == 5)
            liveScoreModel.setOverStatus(OverStatus.COMPLETED.toString());

        if (liveScoreModel.getWicketModel().isWicketStatus() &&
                !liveScoreModel.getWicketModel().getOutType().equals(WicketType.RUNOUT.toString()))
            liveScoreModel.setRuns(0);

        updateScoreBoard(liveScoreModel);
        updateLiveScoreAndCommentary(liveScoreModel);

        if (liveScoreModel.getExtraModel().isExtraStatus())
            updateExtraRuns(liveScoreModel);

        insertNewBowlerToScoreboardOrUpdateExistingBowler(liveScoreModel);

        if (updateAll(liveScoreModel))
            return result(liveScoreModel);
        throw new LiveScoreUpdationException("Invalid data");
    }

    private void doInitialConditions(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException {
        if (liveScoreModel.getOver() == 0 && liveScoreModel.getMatchStatus().equals(MatchStatus.FIRSTINNING.toString())
                && liveScoreModel.getBall() == 1 || liveScoreModel.getBall() == 0) {
            setStatus(liveScoreModel);
            updateScoreBoardStatus(liveScoreModel, MatchStatus.INPROGRESS.toString());
            liveScoreModel.setMatchStatus(MatchStatus.INPROGRESS.toString());
        }
        if (liveScoreModel.getMatchStatus().equals(MatchStatus.SECONDINNING.toString())) {
            jdbcTemplate.update("update matches set matchStatus = ? , totalNumberOfWicket = ? where matchId = ? " +
                            "and tournamentId = ?", MatchStatus.LIVE.toString(), getTotalWicketsForMatch(liveScoreModel),
                    liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
            updateScoreBoardStatus(liveScoreModel, MatchStatus.INPROGRESS.toString());
            liveScoreModel.setMatchStatus(MatchStatus.INPROGRESS.toString());
        }
        List<Matches> tournamentMatches = jdbcTemplate.query("select * from matches where matchId = ?",
                new BeanPropertyRowMapper<>(Matches.class), liveScoreModel.getMatchId());
        if (!tournamentMatches.isEmpty() && tournamentMatches.get(0).getTotalNumberOfWicket() == 0) {
            throw new LiveScoreUpdationException("team does not have wicket to play");
        }
    }

    private void checkValidationBeforeUpdate(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId());
        List<Matches> matches = systemInterface.verifyMatchId(liveScoreModel.getTournamentId(),
                liveScoreModel.getMatchId());
        if (tournament.isEmpty() || matches.isEmpty())
            throw new LiveScoreUpdationException("Invalid Tournament Updation");
        if (liveScoreModel.getRuns() > 7 || liveScoreModel.getRuns() < 0)
            throw new LiveScoreUpdationException("Invalid runs");

        List<Teams> battingTeam = systemInterface.verifyTeamDetails(liveScoreModel.getBattingTeamId(),
                liveScoreModel.getTournamentId());
        List<Teams> bowlingTeam = systemInterface.verifyTeamDetails(liveScoreModel.getBowlingTeamId(),
                liveScoreModel.getTournamentId());
        if (battingTeam.isEmpty() || bowlingTeam.isEmpty() ||
                battingTeam.get(0).getTeamId() == bowlingTeam.get(0).getTeamId())
            throw new LiveScoreUpdationException("Invalid Team");

        List<Players> strikePlayer = getPlayerDetail(liveScoreModel.getStrikeBatsmanId());
        List<Players> nonStrikePlayer = getPlayerDetail(liveScoreModel.getNonStrikeBatsmanId());
        List<Players> bowlingPlayer = getPlayerDetail(liveScoreModel.getBowlerId());
        if (strikePlayer.isEmpty() || nonStrikePlayer.isEmpty() || bowlingPlayer.isEmpty())
            throw new LiveScoreUpdationException("Invalid player");

        if (liveScoreModel.getMatchStatus() != null &&
                liveScoreModel.getMatchStatus().equals(MatchStatus.PAST.toString()))
            throw new LiveScoreUpdationException("Match already completed");
        else if (!getScoreBoard(liveScoreModel).isEmpty() &&
                getScoreBoard(liveScoreModel).get(0).getMatchStatus() != null &&
                (getScoreBoard(liveScoreModel).get(0).getMatchStatus().equals(MatchStatus.INNINGCOMPLETED.toString()) ||
                        getScoreBoard(liveScoreModel).get(0).getMatchStatus().equals(MatchStatus.COMPLETED.toString())))
            throw new LiveScoreUpdationException("Inning completed or match completed");
        List<Matches> match = jdbcTemplate.query("select * from matches where tournamentId = ? and matchStatus = ?",
                new BeanPropertyRowMapper<>(Matches.class), liveScoreModel.getTournamentId(),
                MatchStatus.CANCELLED.toString());
        if (!match.isEmpty())
            throw new LiveScoreUpdationException("All matches does not have ground so you cannot start tournament");
    }

    /**
     * Setting Initial Status
     */
    private void setStatus(LiveScoreUpdateModel liveScoreModel) {
        if (getScoreBoard(liveScoreModel).isEmpty()) {
            jdbcTemplate.update("update matches set matchStatus = ? , totalNumberOfWicket = ? where matchId = ? " +
                            "and tournamentId = ?", MatchStatus.LIVE.toString(), getTotalWicketsForMatch(liveScoreModel),
                    liveScoreModel.getMatchId(), liveScoreModel.getTournamentId());
            jdbcTemplate.update("update tournaments set tournamentStatus = ? where tournamentId = ?",
                    TournamentStatus.PROGRESS.toString(), liveScoreModel.getTournamentId());
            insertIntoScoreBoardOfTeams(liveScoreModel, liveScoreModel.getBattingTeamId(), systemInterface.verifyTeamDetails(
                    liveScoreModel.getBattingTeamId(), liveScoreModel.getTournamentId()).get(0).getTeamName());
            insertIntoScoreBoardOfTeams(liveScoreModel, liveScoreModel.getBowlingTeamId(), systemInterface.verifyTeamDetails(
                    liveScoreModel.getBowlingTeamId(), liveScoreModel.getTournamentId()).get(0).getTeamName());
            Long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                    liveScoreModel.getBattingTeamId());
            insertNewBatsmanToScoreboard(liveScoreModel, scoreBoardId, liveScoreModel.getStrikeBatsmanId(),
                    StrikePosition.STRIKE.toString());
            insertNewBatsmanToScoreboard(liveScoreModel, scoreBoardId, liveScoreModel.getNonStrikeBatsmanId(),
                    StrikePosition.NONSTRIKE.toString());
        }
    }

    private List<ScoreBoard> getScoreBoard(LiveScoreUpdateModel liveScoreModel) {
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), liveScoreModel.getTournamentId(),
                liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
    }

    private void insertIntoScoreBoardOfTeams(LiveScoreUpdateModel liveScoreModel, long teamId, String teamName) {
        jdbcTemplate.update("insert into scoreBoard values(?,?,?,?,?,?,?,?,?,?)", null,
                liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), teamId, teamName, 0, 0, 0, 0, null);
    }

    //get total number of players should play in match
    private int getTotalWicketsForMatch(LiveScoreUpdateModel liveScoreModel) {
        int battingTeamMembers = systemInterface.verifyTeamDetails(liveScoreModel.getBattingTeamId(),
                liveScoreModel.getTournamentId()).get(0).getNumberOfPlayers();
        int bowlingTeamMembers = systemInterface.verifyTeamDetails(liveScoreModel.getBowlingTeamId(),
                liveScoreModel.getTournamentId()).get(0).getNumberOfPlayers();
        return battingTeamMembers > 11 && bowlingTeamMembers > 11 ? 11 :
                Math.min(battingTeamMembers, bowlingTeamMembers) - 1;
    }

    //get scoreboard id
    private Long getScoreBoardId(long tournamentId, long matchId, Long teamId) {
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), tournamentId, matchId, teamId).get(0).getScoreBoardId();
    }

    private void insertNewBatsmanToScoreboard(LiveScoreUpdateModel liveScoreUpdateModel, Long scoreBoardId, Long playerId, String strikePosition) {
        jdbcTemplate.update("insert into batsmanSB values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(), playerId, getPlayerDetail(playerId).get(0).getPlayerName(), 0, 0,
                0, 0, 0, BatsmanStatus.NOTOUT.toString(), strikePosition, null, null);
    }

    private List<Players> getPlayerDetail(Long playerId) {
        return jdbcTemplate.query("select * from players where playerId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Players.class), playerId);
    }

    /**
     * update scoreBoard
     */

    private void updateScoreBoard(LiveScoreUpdateModel liveScoreModel) {
        Long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        if (!liveScoreModel.getExtraModel().isExtraStatus() && liveScoreModel.getBall() < 5)
            jdbcTemplate.update("update scoreBoard set overs = ? , ball = ? , score = score + ? where scoreBoardId = ?",
                    liveScoreModel.getOver(), liveScoreModel.getBall() + 1, liveScoreModel.getRuns(), scoreBoardId);
        else if (!liveScoreModel.getExtraModel().isExtraStatus() && liveScoreModel.getBall() == 5)
            jdbcTemplate.update("update scoreBoard set overs = ? , ball = 0 , score = score + ? where scoreBoardId = ?",
                    liveScoreModel.getOver() + 1, liveScoreModel.getRuns(), scoreBoardId);
        else
            jdbcTemplate.update("update scoreBoard set score = score + ? where scoreBoardId = ?",
                    liveScoreModel.getRuns(), scoreBoardId);
        ScoreBoard scoreBoard = getScoreBoard(liveScoreModel).get(0);
        jdbcTemplate.update("update versus set totalScore = ? , totalOverPlayed = ? , totalballsPlayed = ?" +
                        " where matchId = ? and teamId = ?", scoreBoard.getScore(), scoreBoard.getOvers(),
                scoreBoard.getBall(), scoreBoard.getMatchId(), scoreBoard.getTeamId());
        if (liveScoreModel.getWicketModel().isWicketStatus())
            updateWicketToScoreBoardAndFallOfWicket(liveScoreModel, scoreBoardId);
    }

    private void updateWicketToScoreBoardAndFallOfWicket(LiveScoreUpdateModel liveScoreModel, Long scoreBoardId) {
        jdbcTemplate.update("update scoreBoard set totalWicketFall = totalWicketFall + 1 where scoreBoardId = ?",
                scoreBoardId);
        jdbcTemplate.update("update matches set totalNumberOfWicket = totalNumberOfWicket -1 where matchId = ?",
                liveScoreModel.getMatchId());
        ScoreBoard scoreBoard = getScoreBoard(liveScoreModel).get(0);
        jdbcTemplate.update("insert into fallOfWicketSB values(?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreModel.getBattingTeamId(), liveScoreModel.getWicketModel().getOutPlayerId(),
                getPlayerDetail(liveScoreModel.getWicketModel().getOutPlayerId()).get(0).getPlayerName(),
                scoreBoard.getScore(), scoreBoard.getTotalWicketFall(), scoreBoard.getOvers(), scoreBoard.getBall());
        jdbcTemplate.update("update versus set totalWickets = ? where matchId = ? and teamId = ?",
                scoreBoard.getTotalWicketFall(), scoreBoard.getMatchId(), scoreBoard.getTeamId());
    }

    /**
     * update extra if needed
     */
    private void updateExtraRuns(LiveScoreUpdateModel liveScoreModel) {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
        if (getExtraSB(scoreBoardId).isEmpty())
            jdbcTemplate.update("insert into extraRuns values(?,?,?,?,?,?,?,?)", scoreBoardId,
                    liveScoreModel.getBattingTeamId(), 0, 0, 0, 0, 0, 0);
        jdbcTemplate.update("update extraRuns set " + liveScoreModel.getExtraModel().getExtraType().strip() +
                        " = " + liveScoreModel.getExtraModel().getExtraType().strip().strip() + " + ? " +
                        ", totalExtraRuns = totalExtraRuns + ? where scoreBoardId = ? ", liveScoreModel.getRuns(),
                liveScoreModel.getRuns(), scoreBoardId);
    }

    private List<ExtraRuns> getExtraSB(Long scoreBoardId) {
        return jdbcTemplate.query("select * from extraRuns where scoreBoardId = ?",
                new BeanPropertyRowMapper<>(ExtraRuns.class), scoreBoardId);
    }

    /**
     * insert new bowler into bowling scoreboard
     */
    private void insertNewBowlerToScoreboardOrUpdateExistingBowler(LiveScoreUpdateModel liveScoreModel) {
        Long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(),
                liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
        if (liveScoreModel.getBall() == 1 && !liveScoreModel.getExtraModel().isExtraStatus())
            jdbcTemplate.update("update bowlingSB set bowlerStatus = ? where scoreBoardId = ? and  bowlerStatus = 'DONE'",
                    null, scoreBoardId);
        if (getBowlerSB(liveScoreModel).isEmpty())
            insertNewBowlerToScoreBoard(liveScoreModel, scoreBoardId);
        if (liveScoreModel.getExtraModel().isExtraStatus())
            jdbcTemplate.update("update bowlingSB set runs = runs + ? , balls = ? , overs = overs + ? , wickets = wickets + ? " +
                            "where scoreBoardId = ? and playerId = ?", liveScoreModel.getRuns(), liveScoreModel.getBall(),
                    getOverCounts(liveScoreModel), getRunOutConfirm(liveScoreModel), scoreBoardId, liveScoreModel.getBowlerId());
        else
            jdbcTemplate.update("update bowlingSB set runs = runs + ? , balls = ? , overs = overs + ? , wickets = wickets + ? " +
                            "where scoreBoardId = ? and playerId = ?", liveScoreModel.getRuns(), liveScoreModel.getBall() + 1,
                    getOverCounts(liveScoreModel), getRunOutConfirm(liveScoreModel), scoreBoardId, liveScoreModel.getBowlerId());
        if (!liveScoreModel.getExtraModel().isExtraStatus() && liveScoreModel.getBall() == 5)
            updateEconomyRate(liveScoreModel);
    }

    private int getOverCounts(LiveScoreUpdateModel liveScoreModel) {
        return liveScoreModel.getExtraModel().isExtraStatus() ? 0 : liveScoreModel.getBall() == 5 ? 1 : 0;
    }

    private void updateEconomyRate(LiveScoreUpdateModel liveScoreModel) {
        BowlerSB bowlerSB = getBowlerSB(liveScoreModel).get(0);
        Players player = getPlayerDetail(liveScoreModel.getBowlerId()).get(0);
        if (getPlayerStats(liveScoreModel.getBowlerId()).isEmpty())
            jdbcTemplate.update("insert into playerStats values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    liveScoreModel.getBowlerId(), liveScoreModel.getTournamentId(), liveScoreModel.getBowlingTeamId(),
                    player.getPlayerName(), 0, 0, 0, 0, 0, 0, 0, 0, getBowlingAverage(bowlerSB.getRuns(), bowlerSB.getWickets()),
                    getBowlingEconomy(bowlerSB.getRuns(), bowlerSB.getOvers()),
                    getBowlingStrikeRate(bowlerSB.getOvers() * 6, bowlerSB.getWickets()),
                    getWicketHaul(bowlerSB));
        else
            jdbcTemplate.update("update playerStats set bestBowlingAverage = ? , bestBowlingEconomy = ? , " +
                            "mostFiveWicketsHaul = mostFiveWicketsHaul + ?, bestBowlingStrikeRate = ?  " +
                            "where playerId = ?", getBowlingAverage(bowlerSB.getRuns(), bowlerSB.getWickets()),
                    getBowlingEconomy(bowlerSB.getRuns(), bowlerSB.getOvers()), getWicketHaul(bowlerSB),
                    getBowlingStrikeRate(bowlerSB.getOvers() * 6, bowlerSB.getWickets()),
                    bowlerSB.getPlayerId());
        jdbcTemplate.update("update bowlingSB set bowlerStatus = ? , balls = 0 where scoreBoardId = ? and playerId = ?",
                OverStatus.DONE.toString(), bowlerSB.getScoreBoardId(), liveScoreModel.getBowlerId());
    }

    private void insertNewBowlerToScoreBoard(LiveScoreUpdateModel liveScoreModel, long scoreBoardId) {
        if (liveScoreModel.getExtraModel().isExtraStatus())
            insertTOBowlerSb(liveScoreModel, scoreBoardId, 0);
        else
            insertTOBowlerSb(liveScoreModel, scoreBoardId, 1);
    }

    private void insertTOBowlerSb(LiveScoreUpdateModel liveScoreModel, long scoreBoardId, int ball) {
        jdbcTemplate.update("insert into bowlingSB values(?,?,?,?,?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreModel.getBowlingTeamId(), liveScoreModel.getMatchId(), liveScoreModel.getBowlerId(),
                getPlayerDetail(liveScoreModel.getBowlerId()).get(0).getPlayerName(), 0, 0,
                ball, 0, 0, 0, BowlingStatus.BOWLING.toString());
    }

    private int getRunOutConfirm(LiveScoreUpdateModel liveScoreModel) {
        return liveScoreModel.getWicketModel().isWicketStatus() ?
                liveScoreModel.getExtraModel().isExtraStatus() ?
                        liveScoreModel.getExtraModel().getExtraType().equals(WicketType.RUNOUT.toString()) ?
                                0 : 1 : 1 : 0;
    }

    private List<BowlerSB> getBowlerSB(LiveScoreUpdateModel liveScoreModel) {
        return jdbcTemplate.query("Select * from bowlingSB where scoreBoardId = ? and playerId = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), getScoreBoardId(liveScoreModel.getTournamentId(),
                        liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId()), liveScoreModel.getBowlerId());
    }

    private int getWicketHaul(BowlerSB bowlerSB) {
        return bowlerSB.getWickets() == 5 ? 1 : 0;
    }

    /**
     * update score board , player , playerStats, match, tournament
     */
    private boolean updateAll(LiveScoreUpdateModel liveScoreModel) {
        if (liveScoreModel.getExtraModel().isExtraStatus())
            if (liveScoreModel.getWicketModel().isWicketStatus())
                return updateExtraWithWicket(liveScoreModel);
            else
                return updateWithExtra(liveScoreModel);
        else if (liveScoreModel.getWicketModel().isWicketStatus())
            return updateWithWicket(liveScoreModel);
        else
            return update(liveScoreModel);
    }

    /**
     * update everything with Extra With Wicket
     */
    private boolean updateExtraWithWicket(LiveScoreUpdateModel liveScoreModel) {
        String strikePosition = "";
        if (liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString())) {
            updateBatsmanOutStatusWithWicket(liveScoreModel, 1, liveScoreModel.getRuns());
            if (liveScoreModel.getWicketModel().getOutType().equals(WicketType.RUNOUT.toString())) {
                liveScoreModel.setRuns(liveScoreModel.getRuns() + 1);
                strikePosition = getStrikePosition(liveScoreModel.getRuns(), liveScoreModel.getBall());
            }
        } else if (liveScoreModel.getExtraModel().isExtraStatus()) {
            updateBatsmanOutStatusWithWicket(liveScoreModel, 0, liveScoreModel.getRuns() - 1);
            if (liveScoreModel.getWicketModel().getOutType().equals(WicketType.RUNOUT.toString())) {
                liveScoreModel.setRuns(liveScoreModel.getRuns());
                strikePosition = getStrikePositionForExtra(liveScoreModel.getRuns());
            }
        }
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        BatsmanSB batsmanSB = getBatsmanSB(liveScoreModel);
        doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(strikePosition, liveScoreModel.getStrikeBatsmanId(), scoreBoardId);
        if (strikePosition.equals(StrikePosition.STRIKE.toString()))
            doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(StrikePosition.NONSTRIKE.toString(),
                    liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        else
            doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(StrikePosition.STRIKE.toString(),
                    liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        if (!getPlayerDetail(liveScoreModel.getWicketModel().getNewBatsmanId()).isEmpty() &&
                liveScoreModel.getWicketModel().getNewBatsmanId() != null) {
            setNewBatsmanPosition(batsmanSB.getStrikePosition(), scoreBoardId, liveScoreModel.getWicketModel().getNewBatsmanId());
            setNewBatsmanPosition(null, scoreBoardId, liveScoreModel.getWicketModel().getOutPlayerId());
        } else
            setNewBatsmanPosition(null, scoreBoardId, liveScoreModel.getWicketModel().getOutPlayerId());
        return true;
    }


    private void updateBatsmanOutStatusWithWicket(LiveScoreUpdateModel liveScoreModel, int ball, int runs) {
        jdbcTemplate.update("update batsmanSB set batsmanStatus = ? , outByStatus = ? , outByPlayer = ? ," +
                        " runs = runs + ? , balls = balls + ? where playerId = ?", BatsmanStatus.OUT.toString(),
                liveScoreModel.getWicketModel().getOutType(),
                getPlayerDetail(liveScoreModel.getWicketModel().getFielderId()).get(0).getPlayerName(),
                runs, ball, liveScoreModel.getWicketModel().getOutPlayerId());
        insertIntoPlayerStatsWithWicket(liveScoreModel);
    }

    private void insertIntoPlayerStatsWithWicket(LiveScoreUpdateModel liveScoreModel) {
        BatsmanSB batsmanSB = getBatsmanSB(liveScoreModel);
        jdbcTemplate.update("update playerStats set totalFifties = totalFifties + ? , totalHundreds = totalHundreds + ?," +
                        " battingStrikeRate = ? where playerId = ?", getTotalFifties(batsmanSB), getTotalHundreds(batsmanSB),
                getBattingStrikeRate(batsmanSB.getRuns(), batsmanSB.getBalls()), liveScoreModel.getStrikeBatsmanId());
        jdbcTemplate.update("update players set numberOfTimeHeHasBeenOut = numberOfTimeHeHasBeenOut + 1 ," +
                        "matchesPlayed = matchesPlayed + 1 , totalRuns = totalRuns + ?  where playerId = ? ",
                batsmanSB.getRuns(), liveScoreModel.getWicketModel().getOutPlayerId());
        if (!getPlayerDetail(liveScoreModel.getWicketModel().getNewBatsmanId()).isEmpty())
            insertNewBatsmanToScoreboard(liveScoreModel, batsmanSB.getScoreBoardId(),
                    liveScoreModel.getWicketModel().getNewBatsmanId(), batsmanSB.getStrikePosition());
    }

    private int getTotalHundreds(BatsmanSB batsmanSB) {
        return batsmanSB.getRuns() > 99 ? 1 : 0;
    }

    private Object getTotalFifties(BatsmanSB batsmanSB) {
        return batsmanSB.getRuns() > 49 && batsmanSB.getRuns() < 100 ? 1 : 0;
    }

    private BatsmanSB getBatsmanSB(LiveScoreUpdateModel liveScoreModel) {
        return jdbcTemplate.query("select * from batsmanSB where scoreBoardId = ? and playerId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), getScoreBoardId(liveScoreModel.getTournamentId(),
                        liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId()),
                liveScoreModel.getWicketModel().getOutPlayerId()).get(0);
    }

    /**
     * update everything without Extra With Wicket
     */
    private boolean updateWithWicket(LiveScoreUpdateModel liveScoreModel) {
        updateBatsmanOutStatusWithWicket(liveScoreModel, 1, liveScoreModel.getRuns());
        if (liveScoreModel.getWicketModel().getOutType().equals(WicketType.RUNOUT.toString())) {
            liveScoreModel.setRuns(liveScoreModel.getRuns() + 1);
        }
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        BatsmanSB batsmanSB = getBatsmanSB(liveScoreModel);
        String strikePosition = getStrikePosition(liveScoreModel.getRuns(), liveScoreModel.getBall());
        doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(strikePosition, liveScoreModel.getStrikeBatsmanId(), scoreBoardId);
        if (strikePosition.equals(StrikePosition.STRIKE.toString()))
            doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(StrikePosition.NONSTRIKE.toString(),
                    liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        else
            doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(StrikePosition.STRIKE.toString(),
                    liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        if (!getPlayerDetail(liveScoreModel.getWicketModel().getNewBatsmanId()).isEmpty() &&
                liveScoreModel.getWicketModel().getNewBatsmanId() != null) {
            setNewBatsmanPosition(batsmanSB.getStrikePosition(), scoreBoardId, liveScoreModel.getWicketModel().getNewBatsmanId());
            setNewBatsmanPosition(null, scoreBoardId, liveScoreModel.getWicketModel().getOutPlayerId());
        } else
            setNewBatsmanPosition(null, scoreBoardId, liveScoreModel.getWicketModel().getOutPlayerId());
        return true;
    }

    private void setNewBatsmanPosition(String strikePosition, long scoreBoardId, Long newBatsmanId) {
        jdbcTemplate.update("update batsmanSB set strikePosition = ? where scoreBoardId = ? and playerId = ?",
                strikePosition, scoreBoardId, newBatsmanId);
    }

    private void doStrikeRotationAndUpdateScoreForLegByeOrByeWicket(String strikePosition, Long batsmanId, long scoreBoardId) {
        jdbcTemplate.update("update batsmanSB set strikePosition = ? where scoreBoardId = ? and playerId = ?",
                strikePosition, scoreBoardId, batsmanId);
    }

    /**
     * update everything with Extra Without Wicket
     */
    private boolean updateWithExtra(LiveScoreUpdateModel liveScoreModel) {
        if (liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()))
            updateBatsmanSBForLegByeOrBye(liveScoreModel);
        else
            simplyRotateStrikeForExtra(liveScoreModel);
        return true;
    }

    private void updateBatsmanSBForLegByeOrBye(LiveScoreUpdateModel liveScoreModel) {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
        String strikePosition = getStrikePosition(liveScoreModel.getRuns(), liveScoreModel.getBall());
        doStrikeRotationAndUpdateScoreForLegByeOrBye(liveScoreModel, strikePosition, liveScoreModel.getStrikeBatsmanId(), scoreBoardId);
        if (strikePosition.equals(StrikePosition.STRIKE.toString()))
            doStrikeRotationAndUpdateScoreForLegByeOrBye(liveScoreModel, StrikePosition.NONSTRIKE.toString(),
                    liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        else
            doStrikeRotationAndUpdateScoreForLegByeOrBye(liveScoreModel, StrikePosition.STRIKE.toString(),
                    liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
    }

    private void doStrikeRotationAndUpdateScoreForLegByeOrBye(LiveScoreUpdateModel liveScoreModel, String strikePosition, Long batsmanId, Long scoreBoardId) {
        jdbcTemplate.update("update batsmanSB set balls = balls + 1 , strikeRate = ?, strikePosition = ? where scoreBoardId = ? " +
                        "and playerId = ?", getBattingStrikeRate(liveScoreModel.getRuns(), liveScoreModel.getBall()),
                strikePosition, scoreBoardId, batsmanId);
    }

    private void simplyRotateStrikeForExtra(LiveScoreUpdateModel liveScoreModel) {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        String strikePosition = getStrikePositionForExtra(liveScoreModel.getRuns() - 1);
        doStrikeRotationForNonStrikes(strikePosition, liveScoreModel.getStrikeBatsmanId(), scoreBoardId);
        if (strikePosition.equals(StrikePosition.STRIKE.toString()))
            doStrikeRotationForNonStrikes(StrikePosition.NONSTRIKE.toString(), liveScoreModel.getNonStrikeBatsmanId(),
                    scoreBoardId);
        else
            doStrikeRotationForNonStrikes(StrikePosition.STRIKE.toString(), liveScoreModel.getNonStrikeBatsmanId(),
                    scoreBoardId);
    }

    private String getStrikePositionForExtra(int runs) {
        if (runs % 2 != 0)
            return StrikePosition.NONSTRIKE.toString();
        else
            return StrikePosition.STRIKE.toString();
    }

    private void doStrikeRotationForNonStrikes(String strikePosition, Long batsmanId, long scoreBoardId) {
        jdbcTemplate.update("update batsmanSB set strikePosition = ? where scoreBoardId = ? and playerId = ?",
                strikePosition, scoreBoardId, batsmanId);
    }

    /**
     * update everything without Extra Without Wicket
     */
    private boolean update(LiveScoreUpdateModel liveScoreModel) {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
        String strikePosition = getStrikePosition(liveScoreModel.getRuns(), liveScoreModel.getBall());
        doStrikeRotationAndUpdateScoreForCurrentStrikeBatsman(liveScoreModel, strikePosition);
        if (strikePosition.equals(StrikePosition.STRIKE.toString()))
            doStrikeRotationForNonStrikes(StrikePosition.NONSTRIKE.toString(), liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        else
            doStrikeRotationForNonStrikes(StrikePosition.STRIKE.toString(), liveScoreModel.getNonStrikeBatsmanId(), scoreBoardId);
        return true;
    }

    private void doStrikeRotationAndUpdateScoreForCurrentStrikeBatsman(LiveScoreUpdateModel liveScoreModel, String strikePosition) {
        jdbcTemplate.update("update batsmanSB set runs = runs + ? , fours  = fours + ? , sixes = sixes + ? , " +
                        "strikeRate = ? , balls = balls + 1 , strikePosition = ? where playerId = ?",
                liveScoreModel.getRuns(), isFour(liveScoreModel.getRuns()), isSix(liveScoreModel.getRuns()),
                updatePlayerStats(liveScoreModel), strikePosition, liveScoreModel.getStrikeBatsmanId());
    }

    private double updatePlayerStats(LiveScoreUpdateModel liveScoreModel) {
        BatsmanSB batsmanSB = jdbcTemplate.query("select * from batsmanSB where playerId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), liveScoreModel.getStrikeBatsmanId()).get(0);
        insertIntoPlayerStats(liveScoreModel, isFour(liveScoreModel.getRuns()), isSix(liveScoreModel.getRuns()));
        return getBattingStrikeRate(batsmanSB.getRuns(), batsmanSB.getBalls());
    }

    private void insertIntoPlayerStats(LiveScoreUpdateModel liveScoreModel, int fourCount, int sixCount) {
        List<PlayerStats> playerStat = getPlayerStats(liveScoreModel.getStrikeBatsmanId());
        Players player = getPlayerDetail(liveScoreModel.getStrikeBatsmanId()).get(0);
        if (playerStat.isEmpty())
            jdbcTemplate.update("insert into playerStats values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    liveScoreModel.getStrikeBatsmanId(), liveScoreModel.getTournamentId(),
                    liveScoreModel.getBattingTeamId(), player.getPlayerName(), 0, 0, 0, 0, fourCount, sixCount,
                    0, 0, 0, 0, 0, 0);
        else
            jdbcTemplate.update("update playerStats set totalFours = totalFours + ? , totalSixes = totalSixes + ? , " +
                    "battingStrikeRate = ? where playerId = ?", fourCount, sixCount, getBattingStrikeRate(liveScoreModel.getRuns(),
                    liveScoreModel.getBall()), player.getPlayerId());
    }

    private List<PlayerStats> getPlayerStats(Long playerId) {
        return jdbcTemplate.query("select * from playerStats where playerId = ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), playerId);
    }

    private int isSix(int runs) {
        return runs == 6 ? 1 : 0;
    }

    private int isFour(int runs) {
        return runs == 4 ? 1 : 0;
    }

    private String getStrikePosition(int runs, int currentBall) {
        if (currentBall == 5)
            if (runs % 2 == 0)
                return StrikePosition.NONSTRIKE.toString();
            else
                return StrikePosition.STRIKE.toString();
        else if (runs % 2 != 0)
            return StrikePosition.NONSTRIKE.toString();
        else
            return StrikePosition.STRIKE.toString();
    }

    /**
     * player stats
     */

    private double getBattingStrikeRate(Integer runsScored, Integer numberOfBallFaced) {
        return numberOfBallFaced == 0 ? 0 : runsScored.doubleValue() / numberOfBallFaced.doubleValue() * 100;
    }

    private double getBattingAverage(Integer totalScoreOfBatsman, Integer numberOfTimesHeHasBeenOut) {
        return numberOfTimesHeHasBeenOut == 0 ? totalScoreOfBatsman : totalScoreOfBatsman.doubleValue() / numberOfTimesHeHasBeenOut.doubleValue();
    }

    private double getBowlingStrikeRate(Integer numberBowledDeliveries, Integer numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? 0 : numberBowledDeliveries.doubleValue() / numberOfWicketTaken.doubleValue();
    }

    private double getBowlingAverage(Integer numberOfRunsConceded, Integer numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? numberOfRunsConceded : numberOfRunsConceded.doubleValue() / numberOfWicketTaken.doubleValue();
    }

    private double getBowlingEconomy(Integer numberOfRunsConceded, Integer numberOfOverBowled) {
        return numberOfOverBowled == 0 ? 0 : numberOfRunsConceded.doubleValue() / numberOfOverBowled.doubleValue();
    }

    private double currentRunRate(Integer numberOfOver, Integer totalRuns) {
        return totalRuns.doubleValue() / numberOfOver.doubleValue();
    }

    private double netRunRate(Integer totalScoreInEveryMatch) {
        return totalScoreInEveryMatch.doubleValue() / numberOfOversOfTournament.doubleValue();
    }

    private double requiredRunRate(Integer runNeededToWin, Integer remainingOvers) {
        return runNeededToWin.doubleValue() / remainingOvers.doubleValue();
    }

    /**
     * result formation
     */
    private LiveScoreUpdateModel result(LiveScoreUpdateModel liveScoreModel) throws FixtureGenerationException {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
        if (liveScoreModel.getExtraModel().isExtraStatus() && (
                liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.wide.toString()) ||
                        liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.noBall.toString())))
            return resultForExtra(liveScoreModel);
            //need to be verified
        else if (liveScoreModel.getBall() == 5) {
            if (checkForInningComplete(liveScoreModel)) {
                updatePlayerTables(liveScoreModel);
                if (setTotalRunsInVersusAndCheckForMatchComplete(liveScoreModel))
                    liveScoreModel.setMatchStatus(MatchStatus.COMPLETED.toString());
                else
                    liveScoreModel.setMatchStatus(MatchStatus.INNINGCOMPLETED.toString());
            } else {
                long strikeBatsmanSBId = getBatsmanIds(scoreBoardId, StrikePosition.STRIKE.toString());
                long nonStrikeBatsmanSBId = getBatsmanIds(scoreBoardId, StrikePosition.NONSTRIKE.toString());
                liveScoreModel.setStrikeBatsmanId(strikeBatsmanSBId);
                liveScoreModel.setNonStrikeBatsmanId(nonStrikeBatsmanSBId);
                liveScoreModel.setOver(liveScoreModel.getOver() + 1);
                liveScoreModel.setBall(0);
            }
            return liveScoreModel;
        } else if (checkForInningCompleteNotInLastBall(liveScoreModel)) {
            updatePlayerTables(liveScoreModel);
            if (setTotalRunsInVersusAndCheckForMatchComplete(liveScoreModel))
                liveScoreModel.setMatchStatus(MatchStatus.COMPLETED.toString());
            else
                liveScoreModel.setMatchStatus(MatchStatus.INNINGCOMPLETED.toString());
            if ((liveScoreModel.getRuns()) % 2 != 0) {
                long temp = liveScoreModel.getStrikeBatsmanId();
                liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
                liveScoreModel.setNonStrikeBatsmanId(temp);
            }
            return liveScoreModel;
        } else {
            long strikeBatsmanSBId = getBatsmanIds(scoreBoardId, StrikePosition.STRIKE.toString());
            long nonStrikeBatsmanSBId = getBatsmanIds(scoreBoardId, StrikePosition.NONSTRIKE.toString());
            liveScoreModel.setStrikeBatsmanId(strikeBatsmanSBId);
            liveScoreModel.setNonStrikeBatsmanId(nonStrikeBatsmanSBId);
            liveScoreModel.setBall(liveScoreModel.getBall() + 1);
            return liveScoreModel;
        }
    }

    private boolean checkForInningCompleteNotInLastBall(LiveScoreUpdateModel liveScoreModel) {
        Matches match = systemInterface.verifyMatchId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId()).get(0);
        List<ScoreBoard> scoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ? " +
                        "and matchStatus = 'INNINGCOMPLETED'", new BeanPropertyRowMapper<>(ScoreBoard.class),
                liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
        if (match.getTotalNumberOfWicket() == 0) {
            updateScoreBoardStatus(liveScoreModel, MatchStatus.INNINGCOMPLETED.toString());
            insertIntoVersus(liveScoreModel);
            return true;
        } else {
            ScoreBoard board = getScoreBoard(liveScoreModel).get(0);
            return !scoreBoard.isEmpty() && board.getScore() > scoreBoard.get(0).getScore();
        }
    }


    private long getBatsmanIds(long scoreBoardId, String strikePosition) {
        return jdbcTemplate.query("select * from batsmanSB where strikePosition = ? and scoreBoardId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), strikePosition, scoreBoardId).get(0).getPlayerId();
    }

    private LiveScoreUpdateModel resultForExtra(LiveScoreUpdateModel liveScoreModel) throws FixtureGenerationException {
        if ((liveScoreModel.getRuns() - 1) % 2 != 0) {
            long temp = liveScoreModel.getStrikeBatsmanId();
            liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
            liveScoreModel.setNonStrikeBatsmanId(temp);
        }
        if (checkForInningCompleteNotInLastBall(liveScoreModel)) {
            updatePlayerTables(liveScoreModel);
            if (setTotalRunsInVersusAndCheckForMatchComplete(liveScoreModel))
                liveScoreModel.setMatchStatus(MatchStatus.COMPLETED.toString());
            else
                liveScoreModel.setMatchStatus(MatchStatus.INNINGCOMPLETED.toString());
        }
        return liveScoreModel;
    }

    private boolean checkForInningComplete(LiveScoreUpdateModel liveScoreModel) {
        Tournaments tournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).get(0);
        Matches match = systemInterface.verifyMatchId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId()).get(0);
        List<ScoreBoard> scoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ? " +
                        "and matchStatus = 'INNINGCOMPLETED'", new BeanPropertyRowMapper<>(ScoreBoard.class),
                liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
        if (match.getTotalNumberOfWicket() == 0 || tournament.getNumberOfOvers() == liveScoreModel.getOver() + 1) {
            updateScoreBoardStatus(liveScoreModel, MatchStatus.INNINGCOMPLETED.toString());
            insertIntoVersus(liveScoreModel);
            return true;
        } else {
            ScoreBoard board = getScoreBoard(liveScoreModel).get(0);
            return !scoreBoard.isEmpty() && board.getScore() > scoreBoard.get(0).getScore();
        }
    }

    private void updateScoreBoardStatus(LiveScoreUpdateModel liveScoreModel, String matchStatus) {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        jdbcTemplate.update("update scoreBoard set matchStatus = ? where scoreBoardId = ?", matchStatus, scoreBoardId);
    }

    private void insertIntoVersus(LiveScoreUpdateModel liveScoreModel) {
        ScoreBoard battingScoreBoard = getScoreBoard(liveScoreModel).get(0);
        jdbcTemplate.update("update versus set totalScore = ? , totalOverPlayed = ? , totalballsPlayed = ?," +
                        " matchResult = ? where matchId = ? and teamId = ?", battingScoreBoard.getScore(),
                battingScoreBoard.getOvers() + 1, battingScoreBoard.getBall(), VersusStatus.INNINGCOMPLETE.toString(),
                liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
    }

    private void updatePlayerTables(LiveScoreUpdateModel liveScoreModel) {
        long scoreBoardId = getScoreBoard(liveScoreModel).get(0).getScoreBoardId();
        List<BowlerSB> bowlerSB = jdbcTemplate.query("select * from bowlingSB where wickets > 4 and scoreBoardId = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), scoreBoardId);
        for (BowlerSB bowler : bowlerSB)
            jdbcTemplate.update("update playerStats set mostFiveWicketsHaul = mostFiveWicketsHaul + 1 where playerId = ?",
                    bowler.getPlayerId());
        List<BatsmanSB> batsmanSB = jdbcTemplate.query("select * from batsmanSB where scoreBoardId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), scoreBoardId);
        for (BatsmanSB batsman : batsmanSB) {
            Players player = getPlayerDetail(batsman.getPlayerId()).get(0);
            jdbcTemplate.update("update playerStats set battingAverage = ? where playerId = ?",
                    getBattingAverage(player.getTotalRuns(), player.getNumberOfTimeHeHasBeenOut()), player.getPlayerId());
        }
    }

    private boolean setTotalRunsInVersusAndCheckForMatchComplete(LiveScoreUpdateModel liveScoreModel) throws FixtureGenerationException {
        ScoreBoard battingScoreBoard = getScoreBoard(liveScoreModel).get(0);
        String matchResult = getMatchResult(liveScoreModel, battingScoreBoard);
        if (matchResult != null) {
            jdbcTemplate.update("update versus set matchResult = ? where matchId = ? and teamId = ?", matchResult,
                    liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
            Tournaments tournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).get(0);
            if (tournament.getTournamentType().equals(TournamentTypes.KNOCKOUT.toString()))
                if (matchResult.equals(VersusStatus.WIN.toString()))
                    jdbcTemplate.update("update teams set teamStatus = 'WIN' where teamId = ?",
                            liveScoreModel.getBattingTeamId());
                else jdbcTemplate.update("update teams set teamStatus = 'WIN' where teamId = ?",
                        liveScoreModel.getBowlingTeamId());
            if (matchResult.equals(VersusStatus.WIN.toString())) {
                updateAnotherTeam(liveScoreModel, VersusStatus.LOSS.toString());
                return true;
            } else if (matchResult.equals(VersusStatus.LOSS.toString())) {
                updateAnotherTeam(liveScoreModel, VersusStatus.WIN.toString());
                return true;
            } else if (matchResult.equals(VersusStatus.DRAW.toString())) {
                updateAnotherTeam(liveScoreModel, VersusStatus.DRAW.toString());
                return true;
            }
            setALlAfterMatchComplete(liveScoreModel);
            setTeamInfo(liveScoreModel);
            checkForFinalFixtureGeneration(liveScoreModel);
        }
        return false;
    }

    private void checkForFinalFixtureGeneration(LiveScoreUpdateModel liveScoreModel) throws FixtureGenerationException {
        Tournaments tournament = jdbcTemplate.query("select * from tournaments where tournamentId = ?",
                new BeanPropertyRowMapper<>(Tournaments.class), liveScoreModel.getTournamentId()).get(0);
        if (tournament.getTournamentType().equals(TournamentTypes.LEAGUE.toString()))
            generateForLeague(tournament);
        else if (tournament.getTournamentType().equals(TournamentTypes.KNOCKOUT.toString()))
            generateForKnockout(tournament);
    }

    private void generateForKnockout(Tournaments tournament) throws FixtureGenerationException {
        if (tournament.getTotalRoundRobinMatches() == tournament.getTotalMatchesCompleted()) {
            List<Teams> teams = jdbcTemplate.query("select * from teams where tournamentId = ? and teamStatus = ? " +
                            "and isDeleted = 'false' order by teamId DESC",
                    new BeanPropertyRowMapper<>(Teams.class), tournament.getTournamentId(), TeamStatus.WIN.toString());
            if (teams.isEmpty())
                throw new FixtureGenerationException("There are no teams available to play further matches");
            else if (teams.size() == 1) {
                jdbcTemplate.update(" update tournaments set tournamentStatus = ? where tournamentId = ?",
                        TournamentStatus.COMPLETED.toString(), tournament.getTournamentId());
                return;
            }
            fixtureGenerationInterface.roundRobinGenerationForKnockoutNextMatches(tournament);
        }
    }

    private void generateForLeague(Tournaments tournament) {
        if (tournament.getTotalRoundRobinMatches() == 1 &&
                tournament.getTotalRoundRobinMatches() == tournament.getTotalMatchesCompleted()) {
            jdbcTemplate.update(" update tournaments set tournamentStatus = ? where tournamentId = ?",
                    TournamentStatus.COMPLETED.toString(), tournament.getTournamentId());
            return;
        }
        if (tournament.getTotalRoundRobinMatches() == tournament.getTotalMatchesCompleted())
            if (tournament.getTotalRoundRobinMatches() != 2)
                fixtureGenerationInterface.roundRobinGenerationForKnockoutLeague(tournament.getTournamentId(),
                        TournamentStage.SEMIFINALS.toString());
            else
                fixtureGenerationInterface.roundRobinGenerationForKnockoutLeague(tournament.getTournamentId(),
                        TournamentStage.FINALS.toString());
    }

    private void setALlAfterMatchComplete(LiveScoreUpdateModel liveScoreModel) {
        jdbcTemplate.update("update matches set matchStatus = 'PAST' where tournamentId = ? and matchId = ?",
                liveScoreModel.getTournamentId(), liveScoreModel.getMatchId());
        jdbcTemplate.update("update tournaments set totalMatchesCompleted = totalMatchesCompleted + 1 where " +
                "tournamentId = ?", liveScoreModel.getTournamentId());
    }

    private void setTeamInfo(LiveScoreUpdateModel liveScoreModel) {
        List<Versus> versus = jdbcTemplate.query("select * from versus where matchId = ? ",
                new BeanPropertyRowMapper<>(Versus.class), liveScoreModel.getMatchId());
        if (versus.get(0).getMatchResult().equals(VersusStatus.WIN.toString()))
            updatePointsInTeamTable(versus.get(0), versus.get(1), liveScoreModel);
        else if (versus.get(1).getMatchResult().equals(VersusStatus.WIN.toString()))
            updatePointsInTeamTable(versus.get(1), versus.get(0), liveScoreModel);
        else {
            updatePointsForDraw(versus.get(0), liveScoreModel);
            updatePointsForDraw(versus.get(1), liveScoreModel);
        }
    }

    private void updatePointsInTeamTable(Versus winTeam, Versus loseTeam, LiveScoreUpdateModel liveScoreModel) {
        jdbcTemplate.update("update teams set points = points + 2 , totalMatchesPlayed = totalMatchesPlayed" +
                        "  + 1 , totalWins = totalWins + 1 , teamHighestScore = ? , netRunRate = ? where teamId = ? ",
                getHighestScore(winTeam, liveScoreModel.getTournamentId()), netRunRate(winTeam.getTotalScore()),
                winTeam.getTeamId());
        jdbcTemplate.update("update teams set totalMatchesPlayed = totalMatchesPlayed + 1 , totalLosses = " +
                "totalLosses + 1 ,teamHighestScore = ? , netRunRate = ? where teamId = ? ", getHighestScore(loseTeam,
                liveScoreModel.getTournamentId()), netRunRate(loseTeam.getTotalScore()), loseTeam.getTeamId());
    }

    private void updatePointsForDraw(Versus drawTeam, LiveScoreUpdateModel liveScoreModel) {
        jdbcTemplate.update("update teams set points = points + 1 , totalMatchesPlayed = totalMatchesPlayed" +
                "  + 1 , totalDrawsOrCancelledOrNoResult  = totalDrawsOrCancelledOrNoResult + 1 , " +
                "teamHighestScore = ? , netRunRate = ? where teamId = ? ", getHighestScore(drawTeam,
                liveScoreModel.getTournamentId()), netRunRate(drawTeam.getTotalScore()), drawTeam.getTeamId());
    }

    private int getHighestScore(Versus versus, Long tournamentId) {
        int pastScore = systemInterface.verifyTeamDetails(versus.getTeamId(), tournamentId).get(0).getTeamHighestScore();
        return Math.max(pastScore, versus.getTotalScore());
    }

    private void updateAnotherTeam(LiveScoreUpdateModel liveScoreModel, String matchStatus) {
        jdbcTemplate.update("update versus set matchResult = ? where matchId = ? and teamId = ?", matchStatus,
                liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
    }

    private String getMatchResult(LiveScoreUpdateModel liveScoreModel, ScoreBoard battingTeam) {
        List<ScoreBoard> scoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and " +
                        "matchId = ? and teamId = ?", new BeanPropertyRowMapper<>(ScoreBoard.class),
                liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
        if (scoreBoard.get(0).getMatchStatus() == null)
            return null;
        return scoreBoard.get(0).getMatchStatus().equals(MatchStatus.INNINGCOMPLETED.toString()) ?
                scoreBoard.get(0).getScore() > battingTeam.getScore() ? VersusStatus.WIN.toString() :
                        scoreBoard.get(0).getScore() == battingTeam.getScore() ? VersusStatus.DRAW.toString() :
                                VersusStatus.LOSS.toString() : null;
    }

    /**
     * Live and commentary Score update
     */
    private void updateLiveScoreAndCommentary(LiveScoreUpdateModel liveScoreUpdateModel) {
        Tournaments tournaments = systemInterface.verifyTournamentId(liveScoreUpdateModel.getTournamentId()).get(0);
        Teams strikeTeam = systemInterface.verifyTeamDetails(liveScoreUpdateModel.getBattingTeamId(),
                liveScoreUpdateModel.getTournamentId()).get(0);
        liveScoreModification(tournaments, strikeTeam, liveScoreUpdateModel);
        partnershipScoreModification(liveScoreUpdateModel);
        commentaryScoreModification(liveScoreUpdateModel);
    }

    private List<Live> liveDetails(LiveScoreUpdateModel liveScoreUpdateModel) {
        return jdbcTemplate.query("select * from live where teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Live.class), liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                liveScoreUpdateModel.getTournamentId());
    }

    /**
     * update in live score
     */
    private void liveScoreModification(Tournaments tournaments, Teams teams, LiveScoreUpdateModel liveScoreUpdateModel) {
        List<Live> lives = liveDetails(liveScoreUpdateModel);
        if (lives.isEmpty()) {
            List<Live> firstInning = jdbcTemplate.query("select * from live where matchId = ? and tournamentId = ? and teamId in" +
                            "(select teamId from players where playerId = ?)", new BeanPropertyRowMapper<>(Live.class),
                    liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getBowlerId());
            if (firstInning.isEmpty())
                insertIntoLive(liveScoreUpdateModel, teams, 0);
            else
                insertIntoLive(liveScoreUpdateModel, teams, firstInning.get(0).getRuns() - liveScoreUpdateModel.getRuns());
        } else
            updatingForExistingLiveScore(liveScoreUpdateModel, tournaments);
    }

    private void updatingForExistingLiveScore(LiveScoreUpdateModel liveScoreUpdateModel, Tournaments tournaments) {
        List<Live> newLive = liveDetails(liveScoreUpdateModel);
        List<Live> firstInnings = jdbcTemplate.query("select * from live where matchId = ? and tournamentId = ? and teamId in" +
                        "(select teamId from players where playerId = ?)", new BeanPropertyRowMapper<>(Live.class), liveScoreUpdateModel.getMatchId(),
                liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getBowlerId());
        if (firstInnings.isEmpty()) {
            if (liveScoreUpdateModel.getBall() + 1 == 6) {
                double currentRunRate = currentRunRate(liveScoreUpdateModel.getOver() + 1, newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns());
                updateIntoLive(liveScoreUpdateModel, newLive, currentRunRate, 0, 0);
            } else
                updateIntoLive(liveScoreUpdateModel, newLive, newLive.get(0).getCurrentRunRate(), 0, 0);
        } else {
            int neededRuns = firstInnings.get(0).getRuns() - newLive.get(0).getRuns() - liveScoreUpdateModel.getRuns();
            double currentRunRate = currentRunRate(liveScoreUpdateModel.getOver() + 1, newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns());
            double requiredRunRate = requiredRunRate(newLive.get(0).getRunsNeeded(), tournaments.getNumberOfOvers() - liveScoreUpdateModel.getOver() + 1);
            if (liveScoreUpdateModel.getBall() + 1 == 6)
                updateIntoLive(liveScoreUpdateModel, newLive, currentRunRate, requiredRunRate, neededRuns);
            else
                updateIntoLive(liveScoreUpdateModel, newLive, newLive.get(0).getCurrentRunRate(), newLive.get(0).getRequiredRunRate(), neededRuns);
        }
    }

    private void updateIntoLive(LiveScoreUpdateModel liveScoreUpdateModel, List<Live> newLive, double currentRunRate, double requiredRunRate, int neededRun) {
        DecimalFormat df = new DecimalFormat("#.##");
        int wicket = 0;
        int ball = 1;
        if (liveScoreUpdateModel.getWicketModel().isWicketStatus())
            wicket = 1;
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
            ball = 0;
        if (liveScoreUpdateModel.getBall() + 1 == 6 && !liveScoreUpdateModel.getExtraModel().isExtraStatus())
            jdbcTemplate.update("update live set currentRunRate = ?, requiredRunRate = ?, runs = ?, wickets =?, overs = ?," +
                            "balls = ?, runsNeeded = ? where teamId = ? and matchId = ? and tournamentId = ?", df.format(currentRunRate),
                    df.format(requiredRunRate), newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns(), newLive.get(0).getWickets() + wicket, liveScoreUpdateModel.getOver() + 1,
                    0, neededRun, liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                    liveScoreUpdateModel.getTournamentId());
        else
            jdbcTemplate.update("update live set currentRunRate = ?, requiredRunRate = ?, runs = ?, wickets =?, overs = ?," +
                            "balls = ?, runsNeeded = ? where teamId = ? and matchId = ? and tournamentId = ?", df.format(currentRunRate),
                    df.format(requiredRunRate), newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns(), newLive.get(0).getWickets() + wicket, liveScoreUpdateModel.getOver(),
                    liveScoreUpdateModel.getBall() + ball, neededRun, liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                    liveScoreUpdateModel.getTournamentId());
    }

    private void insertIntoLive(LiveScoreUpdateModel liveScoreUpdateModel, Teams teams, int neededRuns) {
        int wicket = 0;
        int ball = 1;
        if (liveScoreUpdateModel.getWicketModel().isWicketStatus())
            wicket = 1;
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
            ball = 0;
        jdbcTemplate.update("insert into live values(?,?,?,?,?,?,?,?,?,?,?,?)", null, liveScoreUpdateModel.getTournamentId(),
                liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), teams.getTeamName(),
                0, 0, liveScoreUpdateModel.getRuns(), wicket, liveScoreUpdateModel.getOver(), liveScoreUpdateModel.getBall() + ball, neededRuns);
    }

    /**
     * update  in partnership
     */
    private void partnershipScoreModification(LiveScoreUpdateModel liveScoreUpdateModel) {
        List<Partnership> partnerships = partnershipDetails(liveScoreUpdateModel);
        List<Partnership> checkPartnership = checkPartnershipDetails(liveScoreUpdateModel);
        if (partnerships.isEmpty() && checkPartnership.isEmpty())
            addPartnershipDetails(liveScoreUpdateModel);
        else {
            long partnershipId;
            if (!partnerships.isEmpty())
                partnershipId = partnerships.get(0).getPartnershipId();
            else
                partnershipId = checkPartnership.get(0).getPartnershipId();
            List<Partnership> newPartnership = existingPartnershipDetails(partnershipId);
            if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
                if (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                        liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()))
                    updateIntoPartnership(liveScoreUpdateModel, newPartnership,
                            newPartnership.get(0).getTotalPartnershipBalls() + 1, partnershipId);
                else
                    updateIntoPartnership(liveScoreUpdateModel, newPartnership, newPartnership.get(0).getTotalPartnershipBalls(),
                            partnershipId);
            else
                updateIntoPartnership(liveScoreUpdateModel, newPartnership,
                        newPartnership.get(0).getTotalPartnershipBalls() + 1, partnershipId);
        }
    }

    private List<Partnership> existingPartnershipDetails(long partnershipId) {
        return jdbcTemplate.query("select * from partnership where partnershipId = ?",
                new BeanPropertyRowMapper<>(Partnership.class), partnershipId);
    }

    private void updateIntoPartnership(LiveScoreUpdateModel liveScoreUpdateModel, List<Partnership> newPartnership, int numberOfBalls, long partnershipId) {
        jdbcTemplate.update("update partnership set partnershipRuns = ?, totalPartnershipBalls = ? where partnershipId = ?",
                newPartnership.get(0).getPartnershipRuns() + liveScoreUpdateModel.getRuns(), numberOfBalls, partnershipId);
    }

    private void addPartnershipDetails(LiveScoreUpdateModel liveScoreUpdateModel) {
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
            if (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()))
                insertIntoPartnership(liveScoreUpdateModel, 1);
            else
                insertIntoPartnership(liveScoreUpdateModel, 0);
        else
            insertIntoPartnership(liveScoreUpdateModel, 1);
    }

    private void insertIntoPartnership(LiveScoreUpdateModel liveScoreUpdateModel, int ball) {
        jdbcTemplate.update("insert into partnership values(?,?,?,?,?,?,?,?)", null, liveScoreUpdateModel.getTournamentId(),
                liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getStrikeBatsmanId(),
                liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getRuns(), ball);
    }

    private List<Partnership> partnershipDetails(LiveScoreUpdateModel liveScoreUpdateModel) {
        return jdbcTemplate.query("select * from partnership where playerOneId = ? " +
                        "and playerTwoId = ? and teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Partnership.class), liveScoreUpdateModel.getStrikeBatsmanId(),
                liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId(),
                liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getTournamentId());
    }

    private List<Partnership> checkPartnershipDetails(LiveScoreUpdateModel liveScoreUpdateModel) {
        return jdbcTemplate.query("select * from partnership where playerOneId = ? " +
                        "and playerTwoId = ? and teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Partnership.class), liveScoreUpdateModel.getNonStrikeBatsmanId(),
                liveScoreUpdateModel.getStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId(),
                liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getTournamentId());
    }

    /**
     * update commentary
     */
    private void commentaryScoreModification(LiveScoreUpdateModel liveScoreUpdateModel) {
        List<Live> lives = liveDetails(liveScoreUpdateModel);
        String comment = getComment(liveScoreUpdateModel);
        if (liveScoreUpdateModel.getWicketModel().isWicketStatus() && liveScoreUpdateModel.getBall() + 1 != 6) {
            if (liveScoreUpdateModel.getExtraModel().isExtraStatus() && (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.wide.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.noBall.toString())))
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString(), liveScoreUpdateModel.getRuns() - 1, comment);
            else if (liveScoreUpdateModel.getExtraModel().isExtraStatus() && (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString())))
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
            else
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
        } else if (liveScoreUpdateModel.getWicketModel().isWicketStatus()) {
            if (liveScoreUpdateModel.getExtraModel().isExtraStatus() && (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString())))
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.COMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
            else
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.COMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
        } else if (liveScoreUpdateModel.getExtraModel().isExtraStatus() && liveScoreUpdateModel.getBall() + 1 != 6) {
            if (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.wide.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.noBall.toString()))
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString(), liveScoreUpdateModel.getRuns() - 1, comment);
            else if (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()))
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
        } else if (liveScoreUpdateModel.getExtraModel().isExtraStatus()) {
            if (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()))
                insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.COMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
        } else if (liveScoreUpdateModel.getBall() + 1 == 6)
            insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.COMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);
        else
            insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString(), liveScoreUpdateModel.getRuns(), comment);

    }

    /**
     * Generate comments
     */
    private String getComment(LiveScoreUpdateModel liveScoreUpdateModel) {
        Players batsman = getPlayerDetail(liveScoreUpdateModel.getStrikeBatsmanId()).get(0);
        Players bowler = getPlayerDetail(liveScoreUpdateModel.getBowlerId()).get(0);

        if (liveScoreUpdateModel.getWicketModel().isWicketStatus())
            if (liveScoreUpdateModel.getWicketModel().getOutType().equals(WicketType.BOWLED.toString()))
                return "Direct Hit! WHAT A WICKET!";
            else if (liveScoreUpdateModel.getWicketModel().getOutType().equals(WicketType.LBW.toString()))
                return "An appeal for LBW, and the Umpire says YES!";
            else if (liveScoreUpdateModel.getWicketModel().getOutType().equals(WicketType.RUNOUT.toString()))
                return "Lovely throw, that's a run-out";
            else if (liveScoreUpdateModel.getWicketModel().getOutType().equals(WicketType.CAUGHT.toString()))
                return "In the air, fielder coming underneath and taken!";
            else if (liveScoreUpdateModel.getWicketModel().getOutType().equals(WicketType.HITWICKET.toString()))
                return "That's a hit wicket";
            else
                return "Stumped and he's gone!";
        else if (liveScoreUpdateModel.getExtraModel().isExtraStatus()) {
            if ((liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.wide.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.noBall.toString()))
                    && liveScoreUpdateModel.getExtraModel().isExtraStatus())
                return bowler.getPlayerName() + " to " + batsman.getPlayerName() + ", Oh no! It's a " +
                        liveScoreUpdateModel.getExtraModel().getExtraType();
            else if ((liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.bye.toString()) ||
                    liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()))
                    && liveScoreUpdateModel.getExtraModel().isExtraStatus())
                return bowler.getPlayerName() + " to " + batsman.getPlayerName() + ", " +
                        liveScoreUpdateModel.getRuns() + " " + liveScoreUpdateModel.getExtraModel().getExtraType() + "(s)";
        } else {
            if (liveScoreUpdateModel.getRuns() > 0 && liveScoreUpdateModel.getRuns() < 4)
                return bowler.getPlayerName() + " to " + batsman.getPlayerName() + ", " +
                        liveScoreUpdateModel.getRuns() + " run(s),\n Straight down the ground.";
            else if (liveScoreUpdateModel.getRuns() == 4)
                return bowler.getPlayerName() + " to " + batsman.getPlayerName() + ", FOUR runs \n All the way to the boundary off the bat!";
            else if (liveScoreUpdateModel.getRuns() == 6)
                return bowler.getPlayerName() + " to " + batsman.getPlayerName() + ", SIX runs \n THAT'S a HUGE HIT!";
            else
                return bowler.getPlayerName() + " to " + batsman.getPlayerName() + ",  No run. Well bowled.";
        }
        return "Oops!... something went wrong ";
    }

    private void insertIntoCommentary(LiveScoreUpdateModel liveScoreUpdateModel, List<Live> lives, String overStatus, int extraRun, String comment) {
        String ballStatus = liveScoreUpdateModel.getWicketModel().isWicketStatus() ? "WICKET" :
                liveScoreUpdateModel.getExtraModel().isExtraStatus() ? liveScoreUpdateModel.getExtraModel().getExtraType() :
                        String.valueOf(liveScoreUpdateModel.getRuns());
        int ball = 1;
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
            ball = 0;
        jdbcTemplate.update("insert into commentary values(?,?,?,?,?,?,?,?,?,?,?)", null, lives.get(0).getLiveId(),
                liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(),
                liveScoreUpdateModel.getOver(), liveScoreUpdateModel.getBall() + ball, extraRun, ballStatus, overStatus, comment);
    }

    /**
     * Stop match
     */
    @Override
    public SuccessResultModel stopMatch(long matchId, long tournamentId, String reason) {
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        jdbcTemplate.update("update matches set matchStatus = ? , isCancelled = 'true' , cancelledReason = ?" +
                        " where tournamentId = ? and matchId = ?", MatchStatus.ABANDONED.toString(), reason,
                tournamentId, matchId);
        return new SuccessResultModel("Match cancelled Successfully");
    }

}