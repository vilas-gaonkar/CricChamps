package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.entity.*;
import cric.champs.livescorerequestmodels.LiveScoreUpdateModel;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.ExtraRuns;
import cric.champs.model.PlayerStats;
import cric.champs.service.*;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
@Service
public class LiveScoreUpdate
        implements LiveScoreUpdateInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    private int numberOfOversOfTournament;

    private String outPlayerPosition;

    @Override
    public LiveScoreUpdateModel updateLiveScore(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException {
        checkValidationBeforeUpdate(liveScoreModel);
        numberOfOversOfTournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).get(0).getNumberOfOvers();

        if (liveScoreModel.getOver() == 0 && liveScoreModel.getMatchStatus().equals("FIRSTINNING") &&
                liveScoreModel.getBall() == 1 || liveScoreModel.getBall() == 0)
            setStatus(liveScoreModel);

        if (liveScoreModel.getMatchStatus().equals("SECONDINNING"))
            jdbcTemplate.update("update matches set matchStatus = ? , totalNumberOfWicket = ? where matchId = ? and tournamentId = ?",
                    MatchStatus.LIVE.toString(), getTotalWicketsForMatch(liveScoreModel), liveScoreModel.getMatchId(),
                    liveScoreModel.getTournamentId());

        updateScoreBoard(liveScoreModel);

        if (liveScoreModel.getExtraModel().isExtraStatus())
            updateExtraRuns(liveScoreModel);

        insertNewBowlerToScoreboardOrUpdateExistingBowler(liveScoreModel);

        if (updateAll(liveScoreModel))
            return result(liveScoreModel);
        throw new LiveScoreUpdationException("Invalid data");
    }

    private void checkValidationBeforeUpdate(LiveScoreUpdateModel liveScoreModel) throws LiveScoreUpdationException {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId());
        List<Matches> matches = systemInterface.verifyMatchId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId());

        if (tournament.isEmpty() || matches.isEmpty())
            throw new LiveScoreUpdationException("Invalid Tournament Updation");

        if (liveScoreModel.getRuns() > 7 || liveScoreModel.getRuns() < 0)
            throw new LiveScoreUpdationException("Invalid runs");

        List<Teams> battingTeam = systemInterface.verifyTeamDetails(liveScoreModel.getBattingTeamId(), liveScoreModel.getTournamentId());
        List<Teams> bowlingTeam = systemInterface.verifyTeamDetails(liveScoreModel.getBowlingTeamId(), liveScoreModel.getTournamentId());

        if (battingTeam.isEmpty() || bowlingTeam.isEmpty() || battingTeam.get(0).getTeamId() == bowlingTeam.get(0).getTeamId())
            throw new LiveScoreUpdationException("Invalid Team");

        List<Players> strikePlayer = getPlayerDetail(liveScoreModel.getStrikeBatsmanId());

        List<Players> nonStrikePlayer = getPlayerDetail(liveScoreModel.getNonStrikeBatsmanId());

        List<Players> bowlingPlayer = getPlayerDetail(liveScoreModel.getBowlerId());

        if (strikePlayer.isEmpty() || nonStrikePlayer.isEmpty() || bowlingPlayer.isEmpty())
            throw new LiveScoreUpdationException("Invalid player");

        if (liveScoreModel.getMatchStatus() != null && liveScoreModel.getMatchStatus().equals(MatchStatus.PAST.toString()))
            throw new LiveScoreUpdationException("Match already completed");
    }

    /**
     * Setting Initial Status
     */
    private void setStatus(LiveScoreUpdateModel liveScoreModel) {
        if (getScoreBoard(liveScoreModel).isEmpty()) {
            jdbcTemplate.update("update matches set matchStatus = ? , totalNumberOfWicket = ? where matchId = ? and tournamentId = ?",
                    MatchStatus.LIVE.toString(), getTotalWicketsForMatch(liveScoreModel), liveScoreModel.getMatchId(),
                    liveScoreModel.getTournamentId());
            jdbcTemplate.update("update tournaments set tournamentStatus = ? where tournamentId = ?",
                    TournamentStatus.PROGRESS.toString(), liveScoreModel.getTournamentId());
            insertIntoScoreBoardOfTeams(liveScoreModel, liveScoreModel.getBattingTeamId(), systemInterface.verifyTeamDetails(
                    liveScoreModel.getBattingTeamId(), liveScoreModel.getTournamentId()).get(0).getTeamName());
            insertIntoScoreBoardOfTeams(liveScoreModel, liveScoreModel.getBowlingTeamId(), systemInterface.verifyTeamDetails(
                    liveScoreModel.getBowlingTeamId(), liveScoreModel.getTournamentId()).get(0).getTeamName());
            Long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                    liveScoreModel.getBattingTeamId());
            insertNewBatsmanToScoreboard(liveScoreModel, scoreBoardId, liveScoreModel.getStrikeBatsmanId());
            insertNewBatsmanToScoreboard(liveScoreModel, scoreBoardId, liveScoreModel.getNonStrikeBatsmanId());
        }
    }

    private List<ScoreBoard> getScoreBoard(LiveScoreUpdateModel liveScoreModel) {
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), liveScoreModel.getTournamentId(),
                liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId());
    }

    private void insertIntoScoreBoardOfTeams(LiveScoreUpdateModel liveScoreModel, long teamId, String teamName) {
        jdbcTemplate.update("insert into scoreBoard values(?,?,?,?,?,?,?,?,?)", null,
                liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(), teamId, teamName, 0, 0, 0, 0);
    }

    //get total number of players should play in match
    private int getTotalWicketsForMatch(LiveScoreUpdateModel liveScoreModel) {
        int battingTeamMembers = systemInterface.verifyTeamDetails(liveScoreModel.getBattingTeamId(),
                liveScoreModel.getTournamentId()).get(0).getNumberOfPlayers();
        int bowlingTeamMembers = systemInterface.verifyTeamDetails(liveScoreModel.getBowlingTeamId(),
                liveScoreModel.getTournamentId()).get(0).getNumberOfPlayers();
        return battingTeamMembers > 11 && bowlingTeamMembers > 11 ? 11 :
                Math.min(battingTeamMembers, bowlingTeamMembers);
    }

    //get scoreboard id
    private Long getScoreBoardId(long tournamentId, long matchId, Long teamId) {
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), tournamentId, matchId, teamId).get(0).getScoreBoardId();
    }

    private void insertNewBatsmanToScoreboard(LiveScoreUpdateModel liveScoreUpdateModel, Long scoreBoardId, Long playerId) {
        jdbcTemplate.update("insert into batsmanSB values(?,?,?,?,?,?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreUpdateModel.getBattingTeamId(), playerId, getPlayerDetail(playerId).get(0).getPlayerName(), 0, 0,
                0, 0, 0, BatsmanStatus.NOTOUT.toString(), null, null, null);
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
        jdbcTemplate.update("update scoreBoard set overs = ? , ball = ? , score = score + ? where scoreBoardId = ?",
                liveScoreModel.getOver(), liveScoreModel.getBall(), liveScoreModel.getRuns(), scoreBoardId);
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
                liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
        if (liveScoreModel.getBall() == 1 || liveScoreModel.getBall() == 0)
            jdbcTemplate.update("update bowlingSB set bowlerStatus = ? where scoreBoardId = ? and  bowlerStatus = 'DONE'",
                    null, scoreBoardId);
        if (getBowlerSB(liveScoreModel).isEmpty())
            insertNewBowlerToScoreBoard(liveScoreModel);
        jdbcTemplate.update("update bowlingSB set runs = runs + ? , balls = ? , overs = overs + ? , wickets = wickets + ? " +
                        "where scoreBoardId = ? and playerId = ?", liveScoreModel.getRuns(), liveScoreModel.getBall(),
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
                    getBowlingStrikeRate(bowlerSB.getOvers() + 6, bowlerSB.getWickets()),
                    getWicketHaul(bowlerSB));
        else
            jdbcTemplate.update("update playerStats set bestBowlingAverage = ? , bestBowlingEconomy = ? , " +
                            "mostFiveWicketsHaul = mostFiveWicketsHaul + ?, bestBowlingStrikeRate = ? , bowlerStatus = ? " +
                            "where playerId = ?", getBowlingAverage(bowlerSB.getRuns(), bowlerSB.getWickets()),
                    getBowlingEconomy(bowlerSB.getRuns(), bowlerSB.getOvers()), getWicketHaul(bowlerSB),
                    getBowlingStrikeRate(bowlerSB.getOvers() + 6, bowlerSB.getWickets()),
                    OverStatus.DONE.toString(), bowlerSB.getPlayerId());
    }

    private void insertNewBowlerToScoreBoard(LiveScoreUpdateModel liveScoreModel) {
        long scoreBoardId = getScoreBoardId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId(),
                liveScoreModel.getBowlingTeamId());
        jdbcTemplate.update("insert into bowlingSB values(?,?,?,?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreModel.getBowlingTeamId(), liveScoreModel.getBowlerId(),
                getPlayerDetail(liveScoreModel.getBowlerId()).get(0).getPlayerName(), 0, 0,
                liveScoreModel.getBall(), 0, 0, 0, BowlingStatus.BOWLING.toString());
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
                        liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId()), liveScoreModel.getBowlerId());
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
        } else if (liveScoreModel.getExtraModel().isExtraStatus()){
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
        setNewBatsmanPosition(batsmanSB.getStrikePosition(), scoreBoardId, liveScoreModel.getWicketModel().getNewBatsmanId());
        setNewBatsmanPosition(null, scoreBoardId, liveScoreModel.getWicketModel().getOutPlayerId());
        return true;
    }


    private void updateBatsmanOutStatusWithWicket(LiveScoreUpdateModel liveScoreModel, int ball, int runs) {
        jdbcTemplate.update("update batsmanSB set batsmanStatus = ? , outByStatus = ? , outByPlayer = ? ," +
                        " runs = runs + ? , balls = balls + ? , strikePosition = ? where playerId = ?", BatsmanStatus.OUT.toString(),
                liveScoreModel.getWicketModel().getOutType(),
                getPlayerDetail(liveScoreModel.getWicketModel().getFielderId()).get(0).getPlayerName(),
                runs, ball, null, liveScoreModel.getWicketModel().getOutPlayerId());
        insertIntoPlayerStatsWithWicket(liveScoreModel);
    }

    private void insertIntoPlayerStatsWithWicket(LiveScoreUpdateModel liveScoreModel) {
        BatsmanSB batsmanSB = getBatsmanSB(liveScoreModel);
        jdbcTemplate.update("update playerStats set totalFifties = totalFifties + ? , totalHundreds = totalHundreds + ?," +
                        " battingStrikeRate = ? where playerId = ?", getTotalFifties(batsmanSB), getTotalHundreds(batsmanSB),
                getBattingStrikeRate(batsmanSB.getRuns(), batsmanSB.getBalls()), liveScoreModel.getStrikeBatsmanId());
        jdbcTemplate.update("update players set numberOfTimeHeHasBeenOut = numberOfTimeHeHasBeenOut + 1 ," +
                        "matchesPlayed = matchesPlayed + 1 , totalRuns = totalRuns + ? where playerId = ? ",
                batsmanSB.getRuns(), liveScoreModel.getWicketModel().getOutPlayerId());
        insertNewBatsmanToScoreboard(liveScoreModel, batsmanSB.getScoreBoardId(),
                liveScoreModel.getWicketModel().getNewBatsmanId());
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
        setNewBatsmanPosition(batsmanSB.getStrikePosition(), scoreBoardId, liveScoreModel.getWicketModel().getNewBatsmanId());
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

    private double getBattingStrikeRate(int runsScored, int numberOfBallFaced) {
        return numberOfBallFaced == 0 ? 0 : runsScored / numberOfBallFaced * 100;
    }

    private double getBattingAverage(int totalScoreOfBatsman, int numberOfTimesHeHasBeenOut) {
        return numberOfTimesHeHasBeenOut == 0 ? totalScoreOfBatsman : totalScoreOfBatsman / numberOfTimesHeHasBeenOut;
    }

    private double getBowlingStrikeRate(int numberBowledDeliveries, int numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? 0 : numberBowledDeliveries / numberOfWicketTaken;
    }

    private double getBowlingAverage(int numberOfRunsConceded, int numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? numberOfRunsConceded : numberOfRunsConceded / numberOfWicketTaken;
    }

    private double getBowlingEconomy(int numberOfRunsConceded, int numberOfOverBowled) {
        return numberOfOverBowled == 0 ? 0 : numberOfRunsConceded / numberOfOverBowled;
    }

    private double currentRunRate(Integer numberOfOver, Integer totalRuns) {
        return totalRuns.doubleValue() / numberOfOver.doubleValue();
    }

    private double netRunRate(int totalScoreInEveryMatch) {
        return totalScoreInEveryMatch / numberOfOversOfTournament;
    }

    private double requiredRunRate(Integer runNeededToWin, Integer remainingOvers) {
        return runNeededToWin.doubleValue() / remainingOvers.doubleValue();
    }

    /**
     * result formation
     */
    private LiveScoreUpdateModel result(LiveScoreUpdateModel liveScoreModel) {
        if (liveScoreModel.getExtraModel().isExtraStatus())
            return resultForExtra(liveScoreModel);
            //need to be verified
        else if (liveScoreModel.getBall() == 5)
            if (checkForMatchComplete(liveScoreModel)) {
                updateBowlerStats(liveScoreModel);
                if (setTotalRunsInVersus(liveScoreModel))
                    liveScoreModel.setMatchStatus(MatchStatus.PAST.toString());
                else
                    liveScoreModel.setMatchStatus(MatchStatus.INNINGCOMPLETED.toString());
                return liveScoreModel;
            } else {
                if (liveScoreModel.getRuns() % 2 == 0) {
                    long temp = liveScoreModel.getStrikeBatsmanId();
                    liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
                    liveScoreModel.setNonStrikeBatsmanId(temp);
                }
                liveScoreModel.setOver(liveScoreModel.getOver() + 1);
                liveScoreModel.setBall(0);
                return liveScoreModel;
            }
            //verified
        else {
            if (liveScoreModel.getWicketModel().isWicketStatus()) {
                if ((liveScoreModel.getRuns() + 1) % 2 == 1) {
                    long temp = liveScoreModel.getStrikeBatsmanId();
                    liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
                    liveScoreModel.setNonStrikeBatsmanId(temp);
                }
            } else {
                if (liveScoreModel.getRuns() % 2 == 1) {
                    long temp = liveScoreModel.getStrikeBatsmanId();
                    liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
                    liveScoreModel.setNonStrikeBatsmanId(temp);
                }
            }
            liveScoreModel.setBall(liveScoreModel.getBall() + 1);
            return liveScoreModel;
        }
    }

    private LiveScoreUpdateModel resultForExtra(LiveScoreUpdateModel liveScoreModel) {
        if (liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.wide.toString()) ||
                liveScoreModel.getExtraModel().getExtraType().equals(ExtraRunsType.noBall.toString())) {
            if ((liveScoreModel.getRuns() - 1) % 2 != 0) {
                long temp = liveScoreModel.getStrikeBatsmanId();
                liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
                liveScoreModel.setNonStrikeBatsmanId(temp);
            }
            return liveScoreModel;
        } else if ((liveScoreModel.getRuns()) % 2 != 0) {
            long temp = liveScoreModel.getStrikeBatsmanId();
            liveScoreModel.setStrikeBatsmanId(liveScoreModel.getNonStrikeBatsmanId());
            liveScoreModel.setNonStrikeBatsmanId(temp);
        }
        return liveScoreModel;
    }

    private boolean setTotalRunsInVersus(LiveScoreUpdateModel liveScoreModel) {
        ScoreBoard battingScoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), liveScoreModel.getTournamentId(),
                liveScoreModel.getMatchId(), liveScoreModel.getBattingTeamId()).get(0);
        String matchResult = getMatchResult(liveScoreModel, battingScoreBoard);
        jdbcTemplate.update("update versus set totalScore = ? , totalWickets = ? , totalOverPlayed = ? , totalballsPlayed = ?," +
                        " matchResult = ? where matchId = ? and teamId = ?", battingScoreBoard.getScore(), battingScoreBoard.getTotalWicketFall(),
                battingScoreBoard.getOvers(), battingScoreBoard.getBall(), matchResult, liveScoreModel.getMatchId(),
                liveScoreModel.getBattingTeamId());
        if (matchResult.equals(VersusStatus.WIN.toString())) {
            updateAnotherTeam(liveScoreModel, VersusStatus.LOSS.toString());
            return true;
        } else if (matchResult.equals(VersusStatus.LOSS.toString())) {
            updateAnotherTeam(liveScoreModel, VersusStatus.WIN.toString());
            return true;
        } else if (matchResult.equals(VersusStatus.DRAW.toString())) {
            updateAnotherTeam(liveScoreModel, VersusStatus.DRAW.toString());
            return true;
        } else return false;
    }

    private void updateAnotherTeam(LiveScoreUpdateModel liveScoreModel, String matchStatus) {
        jdbcTemplate.update("update versus set matchResult = ? where matchId = ? and teamId = ?", matchStatus,
                liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
    }

    private String getMatchResult(LiveScoreUpdateModel liveScoreModel, ScoreBoard battingTeam) {
        List<ScoreBoard> scoreBoard = jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), liveScoreModel.getTournamentId(),
                liveScoreModel.getMatchId(), liveScoreModel.getBowlingTeamId());
        if (scoreBoard.isEmpty())
            return VersusStatus.INNINGCOMPLETE.toString();
        return scoreBoard.get(0).getScore() > battingTeam.getScore() ? VersusStatus.WIN.toString() :
                scoreBoard.get(0).getScore() == battingTeam.getScore() ? VersusStatus.DRAW.toString() :
                        VersusStatus.LOSS.toString();
    }

    private void updateBowlerStats(LiveScoreUpdateModel liveScoreModel) {
    }

    private boolean checkForMatchComplete(LiveScoreUpdateModel liveScoreModel) {
        Tournaments tournament = systemInterface.verifyTournamentId(liveScoreModel.getTournamentId()).get(0);
        Matches match = systemInterface.verifyMatchId(liveScoreModel.getTournamentId(), liveScoreModel.getMatchId()).get(0);
        if (match.getTotalNumberOfWicket() == 0)
            return true;
        else return tournament.getNumberOfOvers() == liveScoreModel.getOver() + 1;
    }

}