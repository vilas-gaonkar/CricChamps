package cric.champs.customexceptions;

public class EmailValidationException extends Exception {
    public EmailValidationException(String message) {
        super(message);
    }
}
