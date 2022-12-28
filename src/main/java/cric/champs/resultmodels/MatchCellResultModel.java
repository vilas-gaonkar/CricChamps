package cric.champs.resultmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchCellResultModel {

    private long tournamentId;

    private long matchId;

    private Long groundId;

    private int matchNumber;

    private String groundName;

    private String matchStatus;

    private LocalDate matchDate;

    private String matchDay;

    private Time matchStartTime;

    private Time matchEndTime;

    private Long firstTeamId;

    private String firstTeamName;

    private int firstTotalScore;

    private int firstTotalWickets;

    private int firstTotalOverPlayed;

    private int firstTotalBallsPlayed;

    private String firstTeamMatchResult;

    private Long secondTeamId;

    private String secondTeamName;

    private int secondTotalScore;

    private int secondTotalWickets;

    private int secondTotalOverPlayed;

    private int secondTotalBallsPlayed;

    private String secondTeamMatchResult;

    private String cancelledReason;

}
