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

    private int overs;

    private int balls;

    private int runs;

    private String ballStatus;

    private String overStatus;

    private String comment;
}
