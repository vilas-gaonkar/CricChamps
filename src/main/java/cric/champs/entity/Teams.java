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

    private  String teamName;

    @Pattern(regexp = "^[A-Za-z ]+$", message = "Captain's name should only contain alphabets")
    private String captainName;

    @Pattern(regexp = "^[A-Za-z ]+$", message = "City name should only contain alphabets")
    private String city;

    private int numberOfPlayers;

    private int  totalMatchesPlayed;

    private int totalWins;

    private int totalLosses;

    private int totalDrawOrCancelledOrNoResult;

    private int points;

    private double netRunRate;

    private String teamLogo;

    private String isDeleted;

}
