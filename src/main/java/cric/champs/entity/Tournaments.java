package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import java.sql.Time;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Tournaments {

    private long tournamentId;

    private long userId;

    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Tournament name should only contain alphabets")
    private String tournamentName;

    private String tournamentType;

    private String tournamentCode;

    private String tournamentLogo;

    private LocalDate tournamentStartDate;

    private LocalDate tournamentEndDate;

    private Time tournamentStartTime;

    private Time tournamentEndTime;

    private int numberOfTeams;

    private int numberOfOvers;

    private int numberOfGrounds;

    private int numberOfUmpires;

    private int totalRoundRobinMatches;

    private int totalMatchesCompleted;

    private int totalHoursPerMatch;

    private int numberOfOversPerBowler;

    private String tournamentStatus;

}
