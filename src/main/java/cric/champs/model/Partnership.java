package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Partnership {

    private long tournamentId;

    private long matchId;

    private long teamId;

    private  long playerOneId;

    private long playerTwoId;

    private int partnershipRuns;

    private int totalPartnershipBalls;
}
