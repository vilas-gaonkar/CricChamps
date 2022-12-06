package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Live {

    private  long liveId;

    private long tournamentId;

    private long matchId;

    private long teamId;

    private String teamName;

    private double currentRunRate;

    private double requiredRunRate;

    private int partnershipRuns;

    private int totalPartnershipBalls;
}
