package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Players {

    private long playerId;

    private long tournamentId;

    private long teamId;

    private String playerName;

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
