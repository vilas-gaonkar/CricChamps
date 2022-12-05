package cric.champs.customannotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class GenderValidator implements ConstraintValidator<Gender, String> {

    @Override
    public boolean isValid(String gender, ConstraintValidatorContext constraintValidatorContext) {
        List<String> genderList = Arrays.asList("MALE", "FEMALE", "OTHERS", "TRANSGENDER");
        return genderList.contains(gender.toUpperCase());
    }

}
