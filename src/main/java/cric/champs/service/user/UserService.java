package cric.champs.service.user;

import cric.champs.customexceptions.*;
import cric.champs.entity.*;
import cric.champs.livescorerequestmodels.SetDateTimeModel;
import cric.champs.model.GroundPhotos;
import cric.champs.resultmodels.GroundResult;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.resultmodels.TeamResultModel;
import cric.champs.resultmodels.TournamentResultModel;
import cric.champs.security.userdetails.JWTUserDetailsService;
import cric.champs.security.utility.JWTUtility;
import cric.champs.service.AccountStatus;
import cric.champs.service.TournamentStatus;
import cric.champs.service.TournamentTypes;
import cric.champs.service.system.SystemInterface;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements LoginInterface, TournamentInterface, GroundInterface, UmpiresInterface, TeamInterface, PlayerInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SystemInterface systemInterface;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private JWTUserDetailsService jwtUserDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;


    /**
     * ******Login Interface******
     */

    @Override
    public String signIn(String email, String password) throws LoginFailedException, NotVerifiedException {
        try {
            if (systemInterface.verifyEmail(email))
                throw new LoginFailedException("Invalid credentials");
            List<Users> user = systemInterface.getUserDetails(email, AccountStatus.VERIFIED.toString());
            if (user.isEmpty())
                throw new NotVerifiedException("Please verify your account before logging in");
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            final UserDetails userDetail = jwtUserDetailsService.loadUserByUsername(email);
            return "Bearer " + jwtUtility.generateToken(userDetail);
        } catch (BadCredentialsException exception) {
            throw new LoginFailedException("Invalid credentials");
        }
    }

    @Override
    public String refreshToken(HttpServletRequest httpServletRequest) {
        DefaultClaims claims = (DefaultClaims) httpServletRequest.getAttribute("claims");
        Map<String, Object> expectedMap = systemInterface.getMapFromDefaultClaim(claims);
        String refreshToken = jwtUtility.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
        return "Bearer " + refreshToken;
    }

    @Override
    public SuccessResultModel signUp(Users user) throws SignupException {
        try {
            if (!systemInterface.verifyEmail(user.getEmail()))
                throw new NullPointerException("This Email is already registered with Cric Champs");
            jdbcTemplate.update("insert into users values(?,?,?,?,?,?,?,?,?,?,?)", null, user.getUsername(), user.getGender(),
                    user.getEmail(), user.getPhoneNumber(), user.getCity(), user.getProfilePicture(), user.getAge(),
                    passwordEncoder.encode(user.getPassword()), AccountStatus.NOTVERIFIED.toString(), "false");
            return new SuccessResultModel("Your Cric Champs account has been created successfully");
        } catch (Exception exception) {
            throw new SignupException("Failed to register. Please provide valid details");
        }
    }

    @Override
    public SuccessResultModel forgotPassword(String email) throws UsernameNotFoundException, OTPGenerateException {
        if (!systemInterface.verifyEmail(email))
            return systemInterface.forgetOtp(email);
        throw new UsernameNotFoundException("Invalid email");
    }

    @Override
    public boolean resetPassword(int otp, String email) {
        return systemInterface.verifyOtp(otp, email);
    }

    @Override
    public SuccessResultModel changePassword(String newPassword, String confirmPassword) throws UpdateFailedException {
        if (newPassword.equals(confirmPassword)) {
            jdbcTemplate.update("update users set password = ? where userId = ? and isDeleted = 'false'",
                    passwordEncoder.encode(newPassword), systemInterface.getUserId());
            return new SuccessResultModel("Your password has been changed successfully");
        }
        throw new UpdateFailedException("Please re-check your password");
    }

    @Override
    public SuccessResultModel resetPassword(String newPassword, String confirmPassword, String email) throws UpdateFailedException {
        if (newPassword.equals(confirmPassword)) {
            jdbcTemplate.update("update users set password = ? where email = ? and isDeleted = 'false'",
                    passwordEncoder.encode(newPassword), email);
            return new SuccessResultModel("Your password has been reset successfully");
        }
        throw new UpdateFailedException("Please re-check your password");
    }

    @Override
    public SuccessResultModel changeProfilePhoto(String profilePhoto) throws UpdateFailedException {
        if (profilePhoto == null)
            throw new UpdateFailedException("Please select a photo");

        jdbcTemplate.update("update users set profilePicture = ? where userId = ? and isDeleted = 'false'",
                profilePhoto, systemInterface.getUserId());
        return new SuccessResultModel("Your profile photo has been changed successfully");
    }

    @Override
    public SuccessResultModel deleteOldProfilePhoto() {
        Users user = systemInterface.getUserDetailByUserId();
        if (user.getProfilePicture() == null)
            throw new NullPointerException("No profile photo present");
        jdbcTemplate.update("update users set profilePicture = ? where userId = ? and isDeleted = 'false'",
                null, systemInterface.getUserId());
        return new SuccessResultModel("Your Profile Photo has been deleted");
    }

    @Override
    public Users getUserDetails() {

        return jdbcTemplate.query("select * from users where userId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Users.class), systemInterface.getUserId()).get(0);
    }

    /**
     * ******Tournament Interface******
     */

    @Override
    public TournamentResultModel registerTournament(Tournaments tournaments) {
        String tournamentCode = systemInterface.generateTournamentCode();
        if (!systemInterface.verifyTournamentCode(tournamentCode).isEmpty())
            registerTournament(tournaments);

        jdbcTemplate.update("insert into tournaments values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null,
                systemInterface.getUserId(), tournaments.getTournamentName(), tournaments.getTournamentType(),
                tournamentCode, tournaments.getTournamentLogo(), null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0,
                TournamentStatus.UPCOMING.toString());
        return new TournamentResultModel(tournaments.getTournamentName(), tournamentCode, "Tournament successfully created");
    }

    @Override
    public List<Tournaments> getTournamentDetails(int pageSize, int pageNumber) {
        int offSet = pageSize * (pageNumber - 1);
        return jdbcTemplate.query("select * from tournaments where userId = ? limit ? offset ?",
                new BeanPropertyRowMapper<>(Tournaments.class), systemInterface.getUserId(), pageSize, offSet);
    }

    @Override
    public Tournaments getTournament(long tournamentId) {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(tournamentId);
        if (tournament.isEmpty())
            throw new NullPointerException("tournament not found");
        return tournament.get(0);
    }

    @Override
    public Tournaments getDetailsByTournamentCode(String tournamentCode) {
        List<Tournaments> tournament = systemInterface.verifyTournamentCode(tournamentCode);
        if (tournament.isEmpty())
            throw new NullPointerException("Invalid tournament code");
        else
            return tournament.get(0);
    }

    @Override
    public SuccessResultModel cancelTournament(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        jdbcTemplate.update("update tournaments set tournamentStatus = ? where tournamentId = ?",
                TournamentStatus.CANCELLED.toString(), tournamentId);
        return systemInterface.deleteMatches(tournamentId);
    }

    @Override
    public SuccessResultModel setTournamentDate(long tournamentId, LocalDate startDate, LocalDate endDate) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        jdbcTemplate.update("update tournaments set tournamentStartDate = ? , tournamentEndDate = ? where " +
                "tournamentId = ? and tournamentStatus = 'UPCOMING'", startDate, endDate, tournamentId);
        return new SuccessResultModel("date updated successfully");
    }

    @Override
    public SuccessResultModel setTournamentTime(long tournamentId, LocalTime startTime, LocalTime endTime) throws FixtureGenerationException {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(tournamentId);
        if (tournament.isEmpty())
            throw new NullPointerException("Tournament not found");
        if (!systemInterface.validateTime(startTime, endTime, tournament.get(0).getNumberOfOvers()))
            throw new FixtureGenerationException("Insufficient time to generate fixture");
        jdbcTemplate.update("update tournaments set tournamentStartTime = ? , tournamentEndTime = ? where " +
                "tournamentId = ? and tournamentStatus = 'UPCOMING'", startTime, endTime, tournamentId);
        return new SuccessResultModel("Time updated successfully");
    }

    @Override
    public SuccessResultModel setTournamentDateTimes(SetDateTimeModel setDateTimeModel) throws FixtureGenerationException {
        if (!systemInterface.verifyTimeDurationGiven(setDateTimeModel))
            throw new FixtureGenerationException("Insufficient time for fixture generation");
        jdbcTemplate.update("update tournaments set tournamentStartTime = ? , tournamentEndTime = ? , tournamentStartDate = ? , " +
                        "tournamentEndDate = ? where tournamentId = ? and tournamentStatus = 'UPCOMING'", setDateTimeModel.getStartTime(),
                setDateTimeModel.getEndTime(), setDateTimeModel.getStartDate(), setDateTimeModel.getEndDate(),
                setDateTimeModel.getTournamentId());
        return new SuccessResultModel("Date and Time updated successfully");
    }

    @Override
    public SuccessResultModel setTournamentOver(long tournamentId, int numberOfOvers) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        jdbcTemplate.update("update tournaments set numberOfOvers = ? where tournamentId = ?", numberOfOvers,
                tournamentId);
        return new SuccessResultModel("Overs updated successfully");
    }

    /**
     * ******Grounds Interface******
     */

    @Override
    public SuccessResultModel registerGrounds(Grounds ground, List<String> groundPhoto) {
        if (systemInterface.verifyTournamentId(ground.getTournamentId()).isEmpty())
            throw new NullPointerException("Tournament not found");

        if (!systemInterface.verifyLatitudeAndLongitude(ground.getLatitude(), ground.getLongitude(), ground.getTournamentId()).isEmpty())
            throw new NullPointerException("A ground already exists in the given co-ordinates");

        jdbcTemplate.update("insert into grounds values(?,?,?,?,?,?,?,?)", null, ground.getTournamentId(),
                ground.getGroundName(), ground.getCity(), ground.getGroundLocation(), ground.getLatitude(),
                ground.getLongitude(), "false");
        Grounds grounds = jdbcTemplate.query("select * from grounds order by groundId DESC limit 1",
                new BeanPropertyRowMapper<>(Grounds.class)).get(0);
        if (!groundPhoto.isEmpty())
            for (String photo : groundPhoto)
                jdbcTemplate.update("insert into groundPhotos values(?,?)", grounds.getGroundId(), photo);
        jdbcTemplate.update("update tournaments set numberOfGrounds = numberOfGrounds + 1 where tournamentId = ?",
                ground.getTournamentId());
        return new SuccessResultModel("Ground added successfully");
    }

    @Override
    public SuccessResultModel deleteGrounds(long groundId, long tournamentId) {
        if (systemInterface.verifyGroundId(groundId, tournamentId).isEmpty())
            throw new NullPointerException("Ground not found");
        jdbcTemplate.update("update grounds set isDeleted = 'true' where groundId = ?", groundId);
        jdbcTemplate.update("update tournaments set numberOfGrounds = numberOfGrounds - 1 where tournamentId = ?",
                tournamentId);
        return new SuccessResultModel("Ground has been deleted");
    }

    @Override
    public SuccessResultModel editGround(Grounds ground, List<String> groundPhoto) {
        if (systemInterface.verifyGroundId(ground.getGroundId(), ground.getTournamentId()).isEmpty())
            throw new NullPointerException("Invalid ground");
        if (!systemInterface.verifyLatitudeAndLongitude(ground.getLatitude(), ground.getLongitude(), ground.getTournamentId()).isEmpty())
            throw new NullPointerException("latitude and longitude already added");
        jdbcTemplate.update("update grounds set groundName = ? , city = ? , groundLocation = ? , latitude = ? , longitude = ?" +
                        " where groundId = ?", ground.getGroundName(), ground.getCity(), ground.getGroundLocation(),
                ground.getLatitude(), ground.getLongitude(), ground.getGroundId());
        jdbcTemplate.update("delete from groundPhotos where groundId = ?", ground.getGroundId());
        if (!groundPhoto.isEmpty())
            for (String photo : groundPhoto)
                jdbcTemplate.update("insert into groundPhotos values(?,?)", ground.getGroundId(), photo);
        return new SuccessResultModel("Ground details updated successfully");
    }

    @Override
    public GroundResult getAllGrounds(long tournamentId, int pageSize, int pageNumber) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament details");
        int offset = pageSize * (pageNumber - 1);
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ? and isDeleted = 'false'" +
                " limit ? offset ?", new BeanPropertyRowMapper<>(Grounds.class), tournamentId, pageSize, offset);
        List<GroundPhotos> groundPhotos = new ArrayList<>();
        for (Grounds ground : grounds)
            groundPhotos.addAll(jdbcTemplate.query("select * from groundPhotos where groundId = ?",
                    new BeanPropertyRowMapper<>(GroundPhotos.class), ground.getGroundId()));
        return new GroundResult(grounds, groundPhotos);
    }

    @Override
    public GroundResult getGround(long groundId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament details");
        List<Grounds> grounds = jdbcTemplate.query("select * from grounds where tournamentId = ? and groundId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Grounds.class), tournamentId, groundId);
        List<GroundPhotos> groundPhotos = new ArrayList<>();
        for (Grounds ground : grounds)
            groundPhotos.addAll(jdbcTemplate.query("select * from groundPhotos where groundId = ?",
                    new BeanPropertyRowMapper<>(GroundPhotos.class), ground.getGroundId()));
        return new GroundResult(grounds, groundPhotos);
    }

    /**
     * ******Umpires Interface******
     */

    @Override
    public SuccessResultModel registerUmpires(Umpires umpires) {
        if (systemInterface.verifyTournamentId(umpires.getTournamentId()).isEmpty())
            throw new NullPointerException("Tournament not found");
        jdbcTemplate.update("insert into umpires values(?,?,?,?,?,?,?)", null, umpires.getTournamentId(),
                umpires.getUmpireName(), umpires.getCity(), umpires.getPhoneNumber(), umpires.getUmpirePhoto(), "false");
        jdbcTemplate.update("update tournaments set numberOfUmpires = numberOfUmpires + 1 where tournamentId = ?",
                umpires.getTournamentId());
        return new SuccessResultModel("Umpire registered successfully");
    }

    @Override
    public SuccessResultModel deleteUmpires(long umpireId, long tournamentId) {
        if (systemInterface.verifyUmpireDetails(tournamentId, umpireId).isEmpty())
            throw new NullPointerException("Umpire not found");
        jdbcTemplate.update("update umpires set isDeleted = 'true' where umpireId = ?", umpireId);
        jdbcTemplate.update("update tournaments set numberOfUmpires = numberOfUmpires - 1 where tournamentId = ?",
                tournamentId);
        return new SuccessResultModel("Umpire has been removed");
    }

    @Override
    public SuccessResultModel editUmpire(Umpires umpire) {
        if (systemInterface.verifyUmpireDetails(umpire.getTournamentId(), umpire.getUmpireId()).isEmpty())
            throw new NullPointerException("Invalid umpire details");
        jdbcTemplate.update("update umpires set umpireName = ? ,city = ? ,phoneNumber = ? , umpirePhoto = ? where umpireId = ? ",
                umpire.getUmpireName(), umpire.getCity(), umpire.getPhoneNumber(), umpire.getUmpirePhoto(), umpire.getUmpireId());
        return new SuccessResultModel("Umpire details have been updated successfully");
    }

    @Override
    public List<Umpires> getUmpireDetails(long tournamentId, int pageSize, int pageNumber) {
        int offset = pageSize * (pageNumber - 1);
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from umpires where tournamentId = ? and isDeleted = 'false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Umpires.class), tournamentId, pageSize, offset);
    }

    @Override
    public Umpires getUmpire(long umpireId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        if (systemInterface.verifyUmpireDetails(tournamentId, umpireId).isEmpty())
            throw new NullPointerException("Umpire Not Found");
        return jdbcTemplate.query("select * from umpires where tournamentId = ? and umpireId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Umpires.class), tournamentId, umpireId).get(0);
    }

    /**
     * ******Team Interface******
     */

    @Override
    public TeamResultModel registerTeam(Teams teams) throws Exception {
        if (systemInterface.verifyTournamentId(teams.getTournamentId()).get(0).getTournamentType().equalsIgnoreCase(TournamentTypes.INDIVIDUALMATCH.toString())
                && systemInterface.verifyTournamentId(teams.getTournamentId()).get(0).getNumberOfTeams() == 2)
            throw new Exception("Individual match should not contain more than two teams");
        jdbcTemplate.update("insert into teams values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, teams.getTournamentId(),
                teams.getTeamName(), null, teams.getCity(), 0, 0, 0, 0, 0, 0, 0, 0, teams.getTeamLogo(), null, "false");
        Teams team = jdbcTemplate.query("select *  from teams order by teamId desc", new BeanPropertyRowMapper<>(Teams.class)).get(0);
        return new TeamResultModel(team.getTeamId(), team.getTeamName(), "Team created successfully");
    }

    @Override
    public SuccessResultModel deleteTeam(long teamId, long tournamentId) {
        if (systemInterface.verifyTeamDetails(teamId, tournamentId).isEmpty())
            throw new NullPointerException("Team is not found");
        jdbcTemplate.update("update teams set isDeleted = 'true' where teamId = ? and tournamentId = ?", teamId, tournamentId);
        return new SuccessResultModel("Team deleted successfully.");

    }

    @Override
    public SuccessResultModel editTeam(Teams teams) {
        if (systemInterface.verifyTeamDetails(teams.getTeamId(), teams.getTournamentId()).isEmpty())
            throw new NullPointerException("edited failed");
        jdbcTemplate.update("update teams set teamName = ?, city = ?, teamLogo = ? where teamId = ? and tournamentId = ?",
                teams.getTeamName(), teams.getCity(), teams.getTeamLogo(), teams.getTeamId(), teams.getTournamentId());
        return new SuccessResultModel("edited successfully");
    }

    @Override
    public List<Teams> getAllTeams(long tournamentId, int pageSize, int pageNumber) {
        int offSet = pageSize * (pageNumber - 1);
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted='false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Teams.class), tournamentId, pageSize, offSet);
    }

    @Override
    public Teams getTeam(long teamId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        if (systemInterface.verifyTeamDetails(teamId, tournamentId).isEmpty())
            throw new NullPointerException("Team not found.");
        return jdbcTemplate.query("select * from teams where teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Teams.class), teamId, tournamentId).get(0);
    }

    /**
     * ******Player Interface******
     */

    @Override
    public SuccessResultModel registerPlayer(Players players) {
        if (systemInterface.verifyTournamentId(players.getTournamentId()).isEmpty())
            throw new NullPointerException("tournament not found");
        jdbcTemplate.update("insert into players values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, players.getTournamentId(),
                players.getTeamId(), players.getPlayerName(), players.getCity(), players.getPhoneNumber(), players.getProfilePhoto(),
                players.getDesignation(), players.getExpertise(), players.getBattingStyle(), players.getBowlingStyle(),
                players.getBowlingType(), 0, 0, 0, 0, null, players.getPersonalId(), players.getPersonalIdName(), "false");
        jdbcTemplate.update("update teams set numberOfPlayers = numberOfPlayers + 1 where teamId = ?", players.getTeamId());
        return new SuccessResultModel("Player registered successfully.");
    }

    @Override
    public SuccessResultModel deletePlayer(long playerId, long teamId, long tournamentId) {
        if (systemInterface.verifyPlayerDetails(playerId, teamId, tournamentId).isEmpty())
            throw new NullPointerException("Player not found");
        jdbcTemplate.update("update players set isDeleted = 'true' where playerId = ? and teamId = ? and tournamentId = ?",
                playerId, teamId, tournamentId);
        return new SuccessResultModel("Player deleted successfully");

    }

    @Override
    public SuccessResultModel editPlayer(Players players) {
        if (systemInterface.verifyPlayerDetails(players.getPlayerId(), players.getTeamId(), players.getTournamentId()).isEmpty())
            throw new NullPointerException("Player is not found.");
        jdbcTemplate.update("update players set playerName = ?, city = ?, phoneNumber = ?, profilePhoto = ?," +
                        "designation = ?, expertise = ?, battingStyle = ?, bowlingStyle = ?, bowlingType = ? where playerId = ? " +
                        "and teamId = ? and tournamentId = ?", players.getPlayerName(), players.getCity(), players.getPhoneNumber(),
                players.getProfilePhoto(), players.getDesignation(), players.getExpertise(), players.getBattingStyle(),
                players.getBowlingStyle(), players.getBowlingType(), players.getPlayerId(), players.getTeamId(), players.getTournamentId());
        return new SuccessResultModel("Player edited successfully");
    }

    @Override
    public List<Players> getAllPlayers(long teamId, long tournamentId, int pageSize, int pageNumber) {
        int offSet = pageSize * (pageNumber - 1);
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        List<Players> players = systemInterface.verifyTeamAndTournamentId(teamId, tournamentId);
        if (players.isEmpty())
            throw new NullPointerException("Players not found");
        return jdbcTemplate.query("select * from players where teamId = ? and tournamentId = ? and isDeleted = 'false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Players.class), teamId, tournamentId, pageSize, offSet);
    }

    @Override
    public Players getPlayer(long playerId, long teamId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        if (systemInterface.verifyTeamDetails(teamId, tournamentId).isEmpty())
            throw new NullPointerException("Team not found");
        if (systemInterface.verifyPlayerDetails(playerId, teamId, tournamentId).isEmpty())
            throw new NullPointerException("Player not found");
        return jdbcTemplate.query("select * from players where playerId = ? and teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Players.class), playerId, teamId, tournamentId).get(0);
    }

}
