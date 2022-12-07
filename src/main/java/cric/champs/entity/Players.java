package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Players {

    private long playerId;

    private long tournamentId;

    private long teamId;


    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+|[A-Za-z]+$", message = "Player name should only contain alphabets")
    private String playerName;


    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+|[A-Za-z]+$", message = "City name should only contain alphabets")
    private String city;

    private String phoneNumber;

    private String profilePhoto;

    private String designation;

    private String expertise;

    private String battingStyle;

    private String bowlingStyle;

    private String bowlingType;

    private int matchesPlayed;

    private int totalWickets;

    private int totalRuns;

    private String achievements;

    private String personalId;

    private String personalIdName;

    private String isDeleted;
}
