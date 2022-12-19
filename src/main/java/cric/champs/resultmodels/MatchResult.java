package cric.champs.resultmodels;

import cric.champs.entity.Matches;
import cric.champs.model.Versus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResult {
    private Matches matches;

    private List<Versus> versus;

}
