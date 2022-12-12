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
public class Teams {

    private long teamId;

    private long tournamentId;
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Team name should only contain alphabets")
    private String teamName;

    private String captainName;

    private String city;

    private int numberOfPlayers;

    private int totalMatchesPlayed;

    private int totalWins;

    private int totalLosses;

    private int totalDrawOrCancelledOrNoResult;

    private int points;

    private int teamHighestScore;

    private double netRunRate;

    private String teamLogo;

    private String teamStatus;

    private String isDeleted;

}
