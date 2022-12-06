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

    private long groundId;

    private String groundName;

    private int roundNumber;

    private int matchNumber;

    private String matchStatus;

    private String statusDescription;

    private LocalDate matchDate;

    private LocalDate matchDay;

    private Time matchStartTime;

    private Time matchEndTime;

    private String isCancelled;

    private String cancelledReason;

}
