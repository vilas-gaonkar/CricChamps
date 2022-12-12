package cric.champs.resultmodels;

import cric.champs.entity.Grounds;
import cric.champs.model.GroundPhotos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroundResult {

    List<Grounds> grounds;

    List<GroundPhotos> groundPhotos;

}
