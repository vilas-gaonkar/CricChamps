package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Commentary {

    private  long liveId;

    private long tournamentId;

    private long matchId;

    private long teamId;

    private int overs;

    private int balls;

    private String ballStatus;

    private String comment;
}
