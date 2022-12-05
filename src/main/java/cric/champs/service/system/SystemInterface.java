package cric.champs.service.system;

import cric.champs.customexceptions.EmailValidationException;
import cric.champs.entity.*;
import cric.champs.customexceptions.OTPGenerateException;
import io.jsonwebtoken.impl.DefaultClaims;

import java.util.List;
import java.util.Map;

public interface SystemInterface {

    int generateOTP();

    String generateTournamentCode();

    boolean verifyEmail(String email);

    boolean verifyOtp(int otp, String email);

    ResultModel verifyUserAccount(int otp, String email) throws EmailValidationException;

    ResultModel sendOTP(String email) throws OTPGenerateException;

    ResultModel forgetOtp(String email) throws OTPGenerateException;

    long getUserId();

    List<Users> getUserDetails(String email, String accountStatus);

    Users getUserDetailByUserId();

    Map<String, Object> getMapFromDefaultClaim(DefaultClaims claims);

    List<Tournaments> verifyTournamentCode(String tournamentCode);

    ResultModel deleteMatches(long tournamentId);

    List<Tournaments> verifyTournamentId(long tournamentId);

    List<Grounds> verifyLatitudeAndLongitude(double latitude, double longitude, long tournamentId);

    List<Grounds> verifyGroundId(long groundId);

    List<Umpires> verifyUmpireDetails(long tournamentId, long umpireId);

}
