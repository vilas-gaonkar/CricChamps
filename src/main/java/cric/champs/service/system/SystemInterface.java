package cric.champs.service.system;

import cric.champs.customexceptions.EmailValidationException;
import cric.champs.entity.*;
import cric.champs.customexceptions.OTPGenerateException;
import cric.champs.requestmodel.ScoreBoardModel;
import cric.champs.requestmodel.SetDateTimeModel;
import cric.champs.resultmodels.SuccessResultModel;
import io.jsonwebtoken.impl.DefaultClaims;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface SystemInterface {

    int generateOTP();

    String generateTournamentCode();

    boolean verifyEmail(String email);

    boolean verifyOtp(int otp, String email);

    SuccessResultModel verifyUserAccount(int otp, String email) throws EmailValidationException;

    SuccessResultModel sendOTP(String email) throws OTPGenerateException;

    SuccessResultModel forgetOtp(String email) throws OTPGenerateException;

    long getUserId();

    List<Users> getUserDetails(String email, String accountStatus);

    Users getUserDetailByUserId();

    Map<String, Object> getMapFromDefaultClaim(DefaultClaims claims);

    List<Tournaments> verifyTournamentCode(String tournamentCode);

    SuccessResultModel deleteMatches(long tournamentId);

    List<Tournaments> verifyTournamentId(long tournamentId);

    List<Grounds> verifyLatitudeAndLongitude(double latitude, double longitude, long tournamentId);

    List<Grounds> verifyGroundId(long groundId, long tournamentId);

    List<Umpires> verifyUmpireDetails(long tournamentId, long umpireId);

    List<Teams> verifyTeamDetails(long teamId, long tournamentId);

    List<Players> verifyPlayerDetails(long playerId, long teamId, long tournamentId);

    List<Players> verifyTeamAndTournamentId(long teamId, long tournamentId);

    List<Tournaments> verifyUserID();

    boolean verifyTimeDurationGiven(SetDateTimeModel setDateTimeModel);

    List<Matches> verifyMatchId(Long tournamentId, Long matchId);

    boolean validateTime(LocalTime startTime, LocalTime endTime, int numberOfOvers);

    List<Tournaments> verifyTournamentsIdWithOutUserVerification(Long tournamentId);

    Long getScoreBoardId(ScoreBoardModel scoreBoardModel);

    void rejectRequest();

    boolean verifyTokenValidity(String token);

    void deleteExpiredTokens();

    String getTokenFromHeader(HttpServletRequest httpServletRequest);
}
