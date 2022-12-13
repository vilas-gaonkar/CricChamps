package cric.champs.service.scoreboardandlivescore;

import cric.champs.customexceptions.LiveScoreUpdationException;
import cric.champs.entity.*;
import cric.champs.livescorerequestmodels.LiveScoreUpdate;
import cric.champs.model.Commentary;
import cric.champs.model.Partnership;
import cric.champs.model.BatsmanSB;
import cric.champs.model.BowlerSB;
import cric.champs.model.PlayerStats;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.MatchStatus;
import cric.champs.service.OverStatus;
import cric.champs.service.TournamentStatus;
import cric.champs.service.*;
import cric.champs.service.fixture.FixtureGenerationInterface;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@SuppressWarnings("ALL")
@Service
public class LiveScoreService implements LiveInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FixtureGenerationInterface fixtureGenerationInterface;

    @Autowired
    private SystemInterface systemInterface;

    private int numberOfOversOfTournament;

    @Override
    public SuccessResultModel updateLiveScore(LiveScoreUpdate liveScoreUpdateModel) throws LiveScoreUpdationException {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(liveScoreUpdateModel.getTournamentId());
        List<Matches> matches = systemInterface.verifyMatchId(liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId());
        if (tournament.isEmpty() || matches.isEmpty())
            throw new LiveScoreUpdationException("Invalid Tournament Updation");
        if (liveScoreUpdateModel.getOver() > tournament.get(0).getNumberOfOvers())
            throw new LiveScoreUpdationException("Inning completed");
        if (liveScoreUpdateModel.getBall() > 6 && liveScoreUpdateModel.getBall() < 1)
            throw new LiveScoreUpdationException("Invalid ball");
        if (liveScoreUpdateModel.getOver() < 0)
            throw new LiveScoreUpdationException("Inavlid over");
        if (liveScoreUpdateModel.getRuns() > 7 && liveScoreUpdateModel.getRuns() < 0)
            throw new LiveScoreUpdationException("Invalid runs");
        List<Teams> strikeTeam = systemInterface.verifyTeamDetails(liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getTournamentId());
        List<Teams> nonStrikeTeam = jdbcTemplate.query("select * from teams where teamId in (select teamId from versus where matchId = ? and teamId != ?)",
                new BeanPropertyRowMapper<>(Teams.class), liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId());
        if (strikeTeam.isEmpty() || nonStrikeTeam.isEmpty())
            throw new LiveScoreUpdationException("Invalid Team");
        List<Players> strikePlayer = jdbcTemplate.query("select * from players where playerId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(Players.class), liveScoreUpdateModel.getStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId());
        List<Players> nonStrikePlayer = jdbcTemplate.query("select * from players where playerId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(Players.class), liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId());
        List<Players> bowlingPlayer = jdbcTemplate.query("select * from players where playerId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(Players.class), liveScoreUpdateModel.getBowlerId(), nonStrikeTeam.get(0).getTeamId());
        if (strikePlayer.isEmpty() || nonStrikePlayer.isEmpty() || bowlingPlayer.isEmpty())
            throw new LiveScoreUpdationException("Invalid player");
        numberOfOversOfTournament = tournament.get(0).getNumberOfOvers();
        if (liveScoreUpdateModel.getOver() == 0 && (liveScoreUpdateModel.getBall() == 1 || (liveScoreUpdateModel.getExtraModel().isExtraStatus() && liveScoreUpdateModel.getBall() == 0)))
            setStatus(liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), strikeTeam.get(0).getTeamName());
        updateScoreBoard(tournament.get(0), matches.get(0), nonStrikeTeam.get(0), strikeTeam.get(0), liveScoreUpdateModel);
        updateLiveScoreAndCommentry(tournament.get(0), matches.get(0), nonStrikeTeam.get(0), strikeTeam.get(0), liveScoreUpdateModel);
        return new SuccessResultModel("Update successfull");
    }

    private boolean setStatus(Long tournamentId, Long matchId, Long teamId, String teamName) {
        jdbcTemplate.update("update matches set matchStatus = ? where matchId = ? and tournamentId = ?", MatchStatus.LIVE.toString(),
                matchId, tournamentId);
        jdbcTemplate.update("update tournaments set tournamentStatus = ? where tournamentId = ?", TournamentStatus.PROGRESS.toString(),
                tournamentId);
        jdbcTemplate.update("insert into scoreBoard values(?,?,?,?,?,?,?,?,?)", null, tournamentId, matchId,
                teamId, teamName, 0, 0, 0, 0);
        return true;
    }

    /**
     *
     * @param tournaments
     * @param matches
     * @param matchTeams
     * @param teams
     * @param liveScoreUpdateModel
     */
    private void updateLiveScoreAndCommentry(Tournaments tournaments, Matches matches, Teams matchTeams, Teams teams, LiveScoreUpdate liveScoreUpdateModel) {
        liveScoreModification(tournaments, matches, matchTeams, teams, liveScoreUpdateModel);
        partnershipScoreModification(tournaments, matches, matchTeams, teams, liveScoreUpdateModel);
        commentaryScoreModification(tournaments, matches, matchTeams, teams, liveScoreUpdateModel);
    }

    private List<Live> liveDetails(LiveScoreUpdate liveScoreUpdateModel) {
        return jdbcTemplate.query("select * from live where teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Live.class), liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                liveScoreUpdateModel.getTournamentId());
    }

    private void liveScoreModification(Tournaments tournaments, Matches matches, Teams matchTeams, Teams teams, LiveScoreUpdate liveScoreUpdateModel) {
        List<Live> lives = liveDetails(liveScoreUpdateModel);
        DecimalFormat df = new DecimalFormat("#.##");
        if (lives.isEmpty()) {
            List<Live> fristInning = jdbcTemplate.query("select * from live where matchId = ? and tournamentId = ? and teamId in" +
                            "(select teamId from players where playerId = ?)", new BeanPropertyRowMapper<>(Live.class), liveScoreUpdateModel.getMatchId(),
                    liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getBowlerId());
            if (fristInning.isEmpty())
                jdbcTemplate.update("insert into live values(?,?,?,?,?,?,?,?,?,?,?,?)", null, liveScoreUpdateModel.getTournamentId(),
                        liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), teams.getTeamName(),
                        0, 0, liveScoreUpdateModel.getRuns(), 0, liveScoreUpdateModel.getOver(), liveScoreUpdateModel.getBall(), 0);
            else
                jdbcTemplate.update("insert into live values(?,?,?,?,?,?,?,?,?,?,?,?)", null, liveScoreUpdateModel.getTournamentId(),
                        liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), teams.getTeamName(),
                        0, 0, liveScoreUpdateModel.getRuns(), 0, liveScoreUpdateModel.getOver(), liveScoreUpdateModel.getBall(),
                        fristInning.get(0).getRuns() - liveScoreUpdateModel.getRuns());
        } else {
            List<Live> newLive = liveDetails(liveScoreUpdateModel);
            List<Live> fristInnings = jdbcTemplate.query("select * from live where matchId = ? and tournamentId = ? and teamId in" +
                            "(select teamId from players where playerId = ?)", new BeanPropertyRowMapper<>(Live.class), liveScoreUpdateModel.getMatchId(),
                    liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getBowlerId());
            if (fristInnings.isEmpty()) {
                if (liveScoreUpdateModel.getBall() == 6) {
                    double currentRunRate = currentRunRate(liveScoreUpdateModel.getOver() + 1, newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns());
                    jdbcTemplate.update("update live set currentRunRate = ?, requiredRunRate = ?, runs = ?, wickets =?, overs = ?," +
                                    "balls = ?, runsNeeded = ? where teamId = ? and matchId = ? and tournamentId = ?", df.format(currentRunRate),
                            0, newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns(), newLive.get(0).getWickets(), liveScoreUpdateModel.getOver(),
                            liveScoreUpdateModel.getBall(), 0, liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                            liveScoreUpdateModel.getTournamentId());
                } else
                    jdbcTemplate.update("update live set currentRunRate = ?, requiredRunRate = ?, runs = ?, wickets =?, overs = ?," +
                                    "balls = ?, runsNeeded = ? where teamId = ? and matchId = ? and tournamentId = ?", newLive.get(0).getCurrentRunRate(),
                            0, newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns(), newLive.get(0).getWickets(), liveScoreUpdateModel.getOver(),
                            liveScoreUpdateModel.getBall(), 0, liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                            liveScoreUpdateModel.getTournamentId());
            } else {
                int neededRuns = fristInnings.get(0).getRuns() - newLive.get(0).getRuns() - liveScoreUpdateModel.getRuns();
                if (liveScoreUpdateModel.getBall() == 6) {
                    double currentRunRate = currentRunRate(liveScoreUpdateModel.getOver() + 1, newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns());
                    double requiredRunRate = requiredRunRate(newLive.get(0).getRunsNeeded(), tournaments.getNumberOfOvers() - liveScoreUpdateModel.getOver() + 1);
                    jdbcTemplate.update("update live set currentRunRate = ?, requiredRunRate = ?, runs = ?, wickets =?, overs = ?," +
                                    "balls = ?, runsNeeded = ? where teamId = ? and matchId = ? and tournamentId = ?", df.format(currentRunRate),
                            df.format(requiredRunRate), newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns(), newLive.get(0).getWickets(), liveScoreUpdateModel.getOver(),
                            liveScoreUpdateModel.getBall(), neededRuns, liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                            liveScoreUpdateModel.getTournamentId());
                } else
                    jdbcTemplate.update("update live set currentRunRate = ?, requiredRunRate = ?, runs = ?, wickets =?, overs = ?," +
                                    "balls = ?, runsNeeded = ? where teamId = ? and matchId = ? and tournamentId = ?", newLive.get(0).getCurrentRunRate(),
                            newLive.get(0).getRequiredRunRate(), newLive.get(0).getRuns() + liveScoreUpdateModel.getRuns(), newLive.get(0).getWickets(), liveScoreUpdateModel.getOver(),
                            liveScoreUpdateModel.getBall(), neededRuns, liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                            liveScoreUpdateModel.getTournamentId());
            }
        }
    }

    private void partnershipScoreModification(Tournaments tournaments, Matches matches, Teams matchTeams, Teams teams, LiveScoreUpdate liveScoreUpdateModel) {
        List<Partnership> partnerships = partnershipDetails(liveScoreUpdateModel);
        if (partnerships.isEmpty()) {
            addPartnershipDetails(liveScoreUpdateModel);
        } else {
            List<Partnership> newPartnership = partnershipDetails(liveScoreUpdateModel);
            if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
                jdbcTemplate.update("update partnership set partnershipRuns = ?, totalPartnershipBalls = ? where teamId = ? and " +
                                "playerOneId = ? and playerTwoId = ? and matchId = ?", newPartnership.get(0).getPartnershipRuns() + liveScoreUpdateModel.getRuns(),
                        newPartnership.get(0).getTotalPartnershipBalls(), liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getStrikeBatsmanId(),
                        liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getMatchId());
            else
                jdbcTemplate.update("update partnership set partnershipRuns = ?, totalPartnershipBalls = ? where teamId = ? and " +
                                "playerOneId = ? and playerTwoId = ? and matchId = ?", newPartnership.get(0).getPartnershipRuns() + liveScoreUpdateModel.getRuns(),
                        newPartnership.get(0).getTotalPartnershipBalls() + 1, liveScoreUpdateModel.getBattingTeamId(),
                        liveScoreUpdateModel.getStrikeBatsmanId(), liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getMatchId());
        }
    }

    private void addPartnershipDetails(LiveScoreUpdate liveScoreUpdateModel) {
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus())
            insertIntoParnership(liveScoreUpdateModel, 0);
        else
            insertIntoParnership(liveScoreUpdateModel, 1);
    }

    private void insertIntoParnership(LiveScoreUpdate liveScoreUpdateModel, int ball) {
        jdbcTemplate.update("insert into partnership values(?,?,?,?,?,?,?)", liveScoreUpdateModel.getTournamentId(),
                liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getStrikeBatsmanId(),
                liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getRuns(), ball);
    }

    private List<Partnership> partnershipDetails(LiveScoreUpdate liveScoreUpdateModel) {
        return jdbcTemplate.query("select * from partnership where playerOneId = ? " +
                        "and playerTwoId = ? and teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Partnership.class), liveScoreUpdateModel.getStrikeBatsmanId(),
                liveScoreUpdateModel.getNonStrikeBatsmanId(), liveScoreUpdateModel.getBattingTeamId(),
                liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getTournamentId());
    }

    private void commentaryScoreModification(Tournaments tournaments, Matches matches, Teams matchTeams, Teams teams, LiveScoreUpdate liveScoreUpdateModel) {
        List<Live> lives = liveDetails(liveScoreUpdateModel);
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus() || liveScoreUpdateModel.getBall() != 6)
            insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.NOTCOMPLETED.toString());
        else
            insertIntoCommentary(liveScoreUpdateModel, lives, OverStatus.COMPLETED.toString());
    }

    private void insertIntoCommentary(LiveScoreUpdate liveScoreUpdateModel, List<Live> lives, String overStatus) {
        String ballStatus = liveScoreUpdateModel.getExtraModel().isExtraStatus() == true ?
                liveScoreUpdateModel.getExtraModel().getExtraType() : String.valueOf(liveScoreUpdateModel.getRuns());
        jdbcTemplate.update("insert into commentary values(?,?,?,?,?,?,?,?,?)", lives.get(0).getLiveId(),
                liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId(), liveScoreUpdateModel.getBattingTeamId(),
                liveScoreUpdateModel.getOver(), liveScoreUpdateModel.getBall(), ballStatus, overStatus, null);
    }

    private List<Commentary> commentaryDetails(LiveScoreUpdate liveScoreUpdateModel) {
        return jdbcTemplate.query("select * from commentary where teamId = ? and matchId = ? and tournamentId = ?",
                new BeanPropertyRowMapper<>(Commentary.class), liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getMatchId(),
                liveScoreUpdateModel.getTournamentId());
    }

    /**
     * Update scoreboard
     *
     * @param tournaments
     * @param matches
     * @param nonStrikeTeam
     * @param strikeTeam
     * @param scoreboardUpdateModel
     */
    private void updateScoreBoard(Tournaments tournaments, Matches matches, Teams nonStrikeTeam, Teams strikeTeam, LiveScoreUpdate liveScoreUpdateModel) {
        Long scoreBoardId = getScoreBoardId(tournaments.getTournamentId(), matches.getMatchId(),
                liveScoreUpdateModel.getBattingTeamId());
        jdbcTemplate.update("update scoreBoard set over = ? , ball = ? , score = score + ? where scoreBoardId = ?",
                liveScoreUpdateModel.getOver(), liveScoreUpdateModel.getBall(), liveScoreUpdateModel.getRuns(), scoreBoardId);
        if (liveScoreUpdateModel.getExtraModel().isExtraStatus()) {
            updateExtraRuns(liveScoreUpdateModel.getExtraModel().getExtraType(), liveScoreUpdateModel.getRuns(), scoreBoardId);
            updateBowlerSB(liveScoreUpdateModel, scoreBoardId);
            if (liveScoreUpdateModel.getExtraModel().getExtraType().equals(ExtraRunsType.legBye.toString()))
                updateBatsmanSBForLegBye(liveScoreUpdateModel, scoreBoardId);
            else
                jdbcTemplate.update("update batsmanSB set strikePosition = ? where scoreBoardId = ?",
                        getStrikePosition(liveScoreUpdateModel.getRuns() - 1, scoreBoardId,
                                liveScoreUpdateModel.getBall()), scoreBoardId);
        } else {
            updateBatsmanSB(liveScoreUpdateModel, scoreBoardId);
        }
    }


    private Long getScoreBoardId(long tournamentId, long matchId, Long teamId) {
        return jdbcTemplate.query("select * from scoreBoard where tournamentId = ? and matchId = ? and teamId = ?",
                new BeanPropertyRowMapper<>(ScoreBoard.class), tournamentId, matchId, teamId).get(0).getScoreBoardId();
    }

    private List<BowlerSB> getBowlerSB(Long bowlerId) {
        return jdbcTemplate.query("Select * from bowlerSB where playerId = ?",
                new BeanPropertyRowMapper<>(BowlerSB.class), bowlerId);
    }

    private Players getPlayerDetail(Long batsmanId) {
        return jdbcTemplate.query("select * from players where playerId = ?", new BeanPropertyRowMapper<>(Players.class),
                batsmanId).get(0);
    }

    private void updateBatsmanSBForLegBye(LiveScoreUpdate liveScoreUpdateModel, Long scoreBoardId) {
        if (getBatsmanSB(liveScoreUpdateModel.getStrikeBatsmanId()).isEmpty())
            insertNewBatsmanToScoreboard(liveScoreUpdateModel, scoreBoardId);
        else
            jdbcTemplate.update("update batsmanSB set balls = balls + 1 , strikePosition = ? where scoreBoardId = ? " +
                            "and playerId = ?", getStrikePositionForExtra(liveScoreUpdateModel.getRuns()), scoreBoardId,
                    liveScoreUpdateModel.getStrikeBatsmanId());
    }

    private void updateBowlerSB(LiveScoreUpdate liveScoreUpdateModel, Long scoreBoardId) {
        if (getBowlerSB(liveScoreUpdateModel.getBowlerId()).isEmpty())
            insertNewBowlerToScoreBoard(liveScoreUpdateModel, scoreBoardId);
        else
            jdbcTemplate.update("update bowlingSB set runs = runs + ?,overs = overs + ? ,balls = ? where plaeryId = ?",
                    liveScoreUpdateModel.getRuns(), getOverCount(liveScoreUpdateModel.getBall()), liveScoreUpdateModel.getBall(),
                    liveScoreUpdateModel.getBowlerId());
    }

    private int getOverCount(int ball) {
        return ball == 6 ? 1 : 0;
    }

    private void updateBatsmanSB(LiveScoreUpdate liveScoreUpdateModel, Long scoreBoardId) {
        if (getBatsmanSB(liveScoreUpdateModel.getStrikeBatsmanId()).isEmpty()) {
            insertNewBatsmanToScoreboard(liveScoreUpdateModel, scoreBoardId);
            jdbcTemplate.update("update batsmanSB set runs = runs + ? , fours  = fours + ? , sixes = sixes + ? , " +
                            "strikeRate = ? , balls = balls + 1 where plaeryId = ?", liveScoreUpdateModel.getRuns(),
                    isFour(liveScoreUpdateModel.getRuns()), isSix(liveScoreUpdateModel.getRuns()),
                    updatePlayerStats(liveScoreUpdateModel), liveScoreUpdateModel.getStrikeBatsmanId());
        }
    }

    private double updatePlayerStats(LiveScoreUpdate liveScoreUpdateModel) {
        BatsmanSB batsmanSB = jdbcTemplate.query("select * from batsmanSB where playerId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), liveScoreUpdateModel.getStrikeBatsmanId()).get(0);
        insertIntoPlayerStats(liveScoreUpdateModel, liveScoreUpdateModel.getRuns(), isFour(liveScoreUpdateModel.getRuns()),
                isSix(liveScoreUpdateModel.getRuns()));
        return getBattingStrikeRate(batsmanSB.getRuns(), batsmanSB.getBalls());
    }

    private void insertIntoPlayerStats(LiveScoreUpdate liveScoreUpdateModel, int currentRuns, int fourCount, int sixCount) {
        List<PlayerStats> playerStat = jdbcTemplate.query("select * from playerStats where playerId = ?",
                new BeanPropertyRowMapper<>(PlayerStats.class), liveScoreUpdateModel.getStrikeBatsmanId());
        Players player = getPlayerDetail(liveScoreUpdateModel.getStrikeBatsmanId());
        if (playerStat.isEmpty())
            jdbcTemplate.update("insert into playerStats values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    liveScoreUpdateModel.getStrikeBatsmanId(), liveScoreUpdateModel.getTournamentId(),
                    liveScoreUpdateModel.getBattingTeamId(), player.getPlayerName(), 0, 0, 0, 0, fourCount, sixCount,
                    0, 0, 0, 0, 0, 0);
        else
            jdbcTemplate.update("update playerStats set totalFours = totalFours + ? , totalSixes = totalSixes + ? " +
                    "where playerId = ?", fourCount, sixCount, player.getPlayerId());
    }

    private int isSix(int runs) {
        return runs == 6 ? 1 : 0;
    }

    private int isFour(int runs) {
        return runs == 4 ? 1 : 0;
    }

    private String getStrikePositionForExtra(int runs) {
        if (runs % 2 != 0)
            return StrikePosition.NONSTRIKE.toString();
        else
            return StrikePosition.STRIKE.toString();
    }

    private List<BatsmanSB> getBatsmanSB(Long strikeBatsmanId) {
        return jdbcTemplate.query("Select * from batsmanSB where playerId = ?",
                new BeanPropertyRowMapper<>(BatsmanSB.class), strikeBatsmanId);
    }

    private String getStrikePosition(int runs, Long scoreboardId, int currentBall) {
        if (currentBall == 6)
            if (runs % 2 == 0)
                return StrikePosition.NONSTRIKE.toString();
            else
                return StrikePosition.STRIKE.toString();
        else {
            if (runs % 2 != 0)
                return StrikePosition.NONSTRIKE.toString();
            else
                return StrikePosition.STRIKE.toString();
        }
    }

    private void insertNewBatsmanToScoreboard(LiveScoreUpdate liveScoreUpdateModel, Long scoreBoardId) {
        jdbcTemplate.update("insert into batsmanSB values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId(),
                liveScoreUpdateModel.getBattingTeamId(), liveScoreUpdateModel.getStrikeBatsmanId(),
                getPlayerDetail(liveScoreUpdateModel.getStrikeBatsmanId()).getPlayerName(), 0, 1, 0, 0, 0,
                BatsmanStatus.NOTOUT.toString(), getStrikePosition(liveScoreUpdateModel.getRuns(), scoreBoardId,
                        liveScoreUpdateModel.getBall()), null, null);
    }


    private void insertNewBowlerToScoreBoard(LiveScoreUpdate liveScoreUpdateModel, Long scoreBoardId) {
        jdbcTemplate.update("insert into bowlerSB values(?,?,?,?,?,?,?,?,?,?,?,?,?)", scoreBoardId,
                liveScoreUpdateModel.getTournamentId(), liveScoreUpdateModel.getMatchId(),
                getPlayerDetail(liveScoreUpdateModel.getBowlerId()).getTeamId(), liveScoreUpdateModel.getBowlerId(),
                getPlayerDetail(liveScoreUpdateModel.getBowlerId()).getPlayerName(), liveScoreUpdateModel.getRuns(),
                0, liveScoreUpdateModel.getBall(), 0, 0, 0, BowlingStatus.BOWLING.toString());
    }

    private void updateExtraRuns(String extraType, int runs, Long scoreBoardId) {
        jdbcTemplate.update("update set " + extraType.strip() + " = " + extraType.strip() + " + ? where scoreBoardId = ? ",
                runs, scoreBoardId);
    }

    private double getBattingStrikeRate(int runsScored, int numberOfBallFaced) {
        return runsScored / numberOfBallFaced * 100;
    }

    private double getBowlingStrikeRate(int numberBowledDeliveries, int numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? 0 : numberBowledDeliveries / numberOfWicketTaken;
    }

    private double getBattingAverage(int totalScoreOfBatsman, int numberOfTimesHeHasBeenOut) {
        return numberOfTimesHeHasBeenOut == 0 ? 0 : totalScoreOfBatsman / numberOfTimesHeHasBeenOut;
    }

    private double getBowlingAverage(int numberOfRunsConceded, int numberOfWicketTaken) {
        return numberOfWicketTaken == 0 ? 0 : numberOfRunsConceded / numberOfWicketTaken;
    }

    private double getBowlingEconomy(int numberOfRunsConceded, int numberOfOverBowled) {
        return numberOfRunsConceded / numberOfOverBowled;
    }

    private double currentRunRate(Integer numberOfOver, Integer totalRuns) {
        return totalRuns.doubleValue() / numberOfOver.doubleValue();
    }

    private double netRunRatePlayed(int totalScoreInEveryMatch) {
        return totalScoreInEveryMatch / numberOfOversOfTournament;
    }

    private double netRunRateGiven(int totalScoreConceded) {
        return totalScoreConceded / numberOfOversOfTournament;
    }

    private double netRunRate(int totalScoreInEveryMatch, int totalScoreConceded) {
        return netRunRatePlayed(totalScoreInEveryMatch) - netRunRateGiven(totalScoreConceded);
    }

    private double requiredRunRate(Integer runNeededToWin, Integer remainingOvers) {
        return runNeededToWin.doubleValue() / remainingOvers.doubleValue();
    }

}