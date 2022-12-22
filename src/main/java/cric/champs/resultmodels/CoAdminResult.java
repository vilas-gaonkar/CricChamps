package cric.champs.resultmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoAdminResult {

    private String userName;

    private String userPhoto;

    private List<Integer> coAdmin;
}
