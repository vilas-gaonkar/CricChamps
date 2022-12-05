package cric.champs.customannotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = GenderValidator.class)
public @interface Gender {

    public String message() default "Please provide valid gender type.(MALE,FEMALE,OTHERS,TRANSGENDER)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
