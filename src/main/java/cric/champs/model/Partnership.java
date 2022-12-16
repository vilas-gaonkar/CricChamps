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

    private long partnershipId;

    private long liveId;

    private int partnershipRuns;

    private int totalPartnershipBalls;
}
