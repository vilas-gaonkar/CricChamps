package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Tournaments {

    private long tournamentId;

    private long userId;

    private String tournamentName;

    private String tournamentType;

    private String tournamentCode;

    private String tournamentLogo;

    private Date tournamentStartDate;

    private Date tournamentEndDate;

    private Time tournamentStartTime;

    private Time tournamentEndTime;

    private int numberOfTeams;

    private int numberOfOvers;

    private int numberOfGrounds;

    private int numberOfUmpires;

    private String tournamentStatus;

    private String isDeleted;
}
