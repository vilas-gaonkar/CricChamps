package cric.champs.service.system;

import cric.champs.customexceptions.EmailValidationException;
import cric.champs.customexceptions.OTPGenerateException;
import cric.champs.entity.*;
import cric.champs.service.AccountStatus;
import cric.champs.service.MatchStatus;
import cric.champs.service.TournamentStatus;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class SystemService implements SystemInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public int generateOTP() {
        return new Random().nextInt(100000, 1000000);
    }

    @Override
    public String generateTournamentCode() {
        Random random = new Random();
        StringBuilder id = new StringBuilder();
        for (int index = 0; index < 4; index++) {
            char character = (char) (65 + random.nextInt(26));
            try {
                id.append(character);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        for (int index = 0; index < 4; index++) {
            char number = (char) (48 + random.nextInt(10));
            try {
                id.append(number);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return id.toString();
    }

    @Override
    public boolean verifyEmail(String email) {
        return jdbcTemplate.query("select * from users where email = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Users.class), email).isEmpty();
    }

    @Override
    public boolean verifyOtp(int otp, String email) {
        OTPManager otpManager = getOtp(email).get(0);
        return otp == otpManager.getOtp() && otpManager.getExpireAt().isAfter(LocalDateTime.now());
    }

    @Override
    public ResultModel verifyUserAccount(int otp, String email) throws EmailValidationException {
        if (verifyOtp(otp, email)) {
            jdbcTemplate.update("update users set accountStatus = ?", AccountStatus.VERIFIED.toString());
            return new ResultModel("Email verified successfully");
        }
        throw new EmailValidationException("Incorrect OTP");
    }

    private List<OTPManager> getOtp(String email) {
        return jdbcTemplate.query("Select * from otpManager where email = ?",
                new BeanPropertyRowMapper<>(OTPManager.class), email);
    }

    @Override
    public ResultModel sendOTP(String userEmail) throws OTPGenerateException {
        List<Users> user = getUserDetails(userEmail, AccountStatus.NOTVERIFIED.toString());

        if (user.isEmpty())
            throw new OTPGenerateException("enter valid registered email");
        List<OTPManager> otpManager = getOtp(userEmail);
        int otp = generateOTP();
        if (otpManager.isEmpty())
            jdbcTemplate.update("Insert into OTPManager values (? , ? , ? , ?)", user.get(0).getUserId(), userEmail,
                    otp, LocalDateTime.now().plusMinutes(5));
        else
            jdbcTemplate.update("update OTPManager set otp = ?, expireAt = ? where email = ?", otp,
                    LocalDateTime.now().plusMinutes(5), userEmail);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("raspberrypi001025@gmail.com");
        email.setTo(userEmail);
        email.setSubject("Cric Champs Registration OTP");
        email.setText("Please enter the following OTP in your Cric Champs App to verify your account: \n" + otp);
        javaMailSender.send(email);

        return new ResultModel("OTP sent Successfully");
    }

    @Override
    public ResultModel forgetOtp(String userEmail) throws OTPGenerateException {
        List<Users> user = getUserDetails(userEmail, AccountStatus.VERIFIED.toString());

        if (user.isEmpty())
            throw new OTPGenerateException("enter valid registered email");
        List<OTPManager> otpManager = getOtp(userEmail);
        int otp = generateOTP();
        if (otpManager.isEmpty())
            jdbcTemplate.update("Insert into OTPManager values (? , ? , ? , ?)", user.get(0).getUserId(), userEmail,
                    otp, LocalDateTime.now().plusMinutes(5));
        else
            jdbcTemplate.update("update OTPManager set otp = ?, expireAt = ? where email = ?", otp,
                    LocalDateTime.now().plusMinutes(5), userEmail);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("raspberrypi001025@gmail.com");
        email.setTo(userEmail);
        email.setSubject("Cric champs registration OTP");
        email.setText("Enter otp in Cric Champs application to verify the account\n" + otp);
        javaMailSender.send(email);

        return new ResultModel("OTP sent Successfully");
    }

    @Override
    public long getUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getUserDetails(userDetails.getUsername(), AccountStatus.VERIFIED.toString()).get(0).getUserId();
    }

    @Override
    public List<Users> getUserDetails(String email, String accountStatus) {
        return jdbcTemplate.query("select * from users where email = ? and accountStatus = ?" +
                " and isDeleted = 'false'", new BeanPropertyRowMapper<>(Users.class), email, accountStatus);
    }

    @Override
    public Users getUserDetailByUserId() {
        return jdbcTemplate.query("select * from users where userId = ?  and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Users.class), getUserId()).get(0);
    }

    @Override
    public Map<String, Object> getMapFromDefaultClaim(DefaultClaims claims) {
        return new HashMap<>(claims);
    }

    @Override
    public List<Tournaments> verifyTournamentCode(String tournamentCode) {
        return jdbcTemplate.query("select * from tournaments where tournamentCode = ? and tournamentStatus != ?",
                new BeanPropertyRowMapper<>(Tournaments.class), tournamentCode, TournamentStatus.CANCELLED.toString());
    }

    @Override
    public ResultModel deleteMatches(long tournamentId) {
        rejectRequest();
        jdbcTemplate.update("update matches set isCancelled = ? where tournamentId = ? ",
                MatchStatus.ABANDONED.toString(), tournamentId);
        return new ResultModel("Cancelled successfully");
    }

    @Override
    public List<Tournaments> verifyTournamentId(long tournamentId) {
        rejectRequest();
        return jdbcTemplate.query("select * from tournaments where tournamentId = ? and userId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Tournaments.class), tournamentId, getUserId());
    }

    @Override
    public List<Grounds> verifyLatitudeAndLongitude(double latitude, double longitude, long tournamentId) {
        rejectRequest();
        return jdbcTemplate.query("select * from grounds where latitude = ? and longitude = ? and tournamentId = ? " +
                "and isDeleted = 'false'", new BeanPropertyRowMapper<>(Grounds.class), latitude, longitude, tournamentId);
    }

    //verify user tournament accounts
    @Override
    public List<Tournaments> verifyUserID() {
        return jdbcTemplate.query("select * from tournaments where userId = ?",
                new BeanPropertyRowMapper<>(Tournaments.class), getUserId());
    }

    @Override
    public List<Teams> verifyTeamDetails(long teamId, long tournamentId) {
        return jdbcTemplate.query("select * from teams where teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Teams.class), teamId, tournamentId);
    }

    @Override
    public List<Players> verifyPlayerDetails(long playerId, long teamId, long tournamentId) {
        if (verifyTournamentId(tournamentId).isEmpty())
            return null;
        return jdbcTemplate.query("select * from players where playerId = ? and teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Players.class), playerId, teamId, tournamentId);
    }

    @Override
    public List<Players> verifyTeamAndTournamentId(long teamId, long tournamentId) {
        return jdbcTemplate.query("select * from players where teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Players.class), teamId, tournamentId);
    }

    @Override
    public List<Grounds> verifyGroundId(long groundId, long tournamentId) {
        rejectRequest();
        return jdbcTemplate.query("select * from grounds where groundId = ? and isDeleted = 'false' and tournamentId = ?",
                new BeanPropertyRowMapper<>(Grounds.class), groundId, tournamentId);
    }

    private void rejectRequest() {
        if (verifyUserID().isEmpty())
            throw new NullPointerException("Access denied");
    }

    @Override
    public List<Umpires> verifyUmpireDetails(long tournamentId, long umpireId) {
        rejectRequest();
        return jdbcTemplate.query("select * from umpires where umpireId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Umpires.class), umpireId, tournamentId);
    }

}
