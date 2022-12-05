package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Teams {

    private long teamId;

    private long tournamentId;

    private  String teamName;

    private String captainName;

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
