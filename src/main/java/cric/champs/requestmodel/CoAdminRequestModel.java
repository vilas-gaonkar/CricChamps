package cric.champs.requestmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoAdminRequestModel {

    private long tournamentUserId;

    private String coAdminEmail;

    private long matchId;
}
