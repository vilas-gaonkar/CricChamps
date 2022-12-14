package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Matches {

    private long matchId;

    private long tournamentId;

    private Long groundId;

    private String groundName;

    private Long umpireId;

    private String umpireName;

    private int roundNumber;

    private int matchNumber;

    private String matchStatus;

    private LocalDate matchDate;

    private String  matchDay;

    private Time matchStartTime;

    private Time matchEndTime;

    private int totalNumberOfWicket;

    private String isCancelled;

    private String cancelledReason;

}
