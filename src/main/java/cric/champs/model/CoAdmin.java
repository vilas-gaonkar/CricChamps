package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoAdmin {

    private Long userId;

    private Long tournamentId;

    private Long matchId;

    private Integer matchNumber;

    private String matchStatus;

    private String isDeleted;
}
