package cric.champs.setting.sidepanelservice;


import cric.champs.entity.Users;
import cric.champs.service.system.SystemInterface;
import cric.champs.setting.model.HelpAndFAQs;
import cric.champs.setting.model.RatingDetails;
import cric.champs.setting.model.Ratings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class SidePanelService implements RatingsInterface, HelpAndFAQsInterface {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public String rating(Integer numberOfStarsRated, String feedback) {
        if (numberOfStarsRated <= 5 && numberOfStarsRated >= 0) {
            Users user = systemInterface.getUserDetailByUserId();
            if (user == null)
                throw new NullPointerException("Sorry you are not registered. Please register.");
            List<Ratings> ratings = jdbcTemplate.query("select * from ratings where userId = ?",
                    new BeanPropertyRowMapper<>(Ratings.class), user.getUserId());
            if (ratings.isEmpty())
                jdbcTemplate.update("insert into ratings values(?, ?, ?)", systemInterface.getUserId(),
                        numberOfStarsRated, feedback);
            jdbcTemplate.update("update ratings set numberOfStarsRated = ?, feedback = ? where userId = ?",
                    numberOfStarsRated, feedback, user.getUserId());
            rateTheApp(ratings.size());
            return "Thank you for Rating!";
        }
        throw new NullPointerException("Please rate between 1 to 5");
    }

    public double calculateRatings(double totalResponse) {
        DecimalFormat formats = new DecimalFormat("0.0");
        int stars5 = jdbcTemplate.query("select * from ratings where numberOfStarsRated = 5",
                new BeanPropertyRowMapper<>(Ratings.class)).size();
        int stars4 = jdbcTemplate.query("select * from ratings where numberOfStarsRated = 4",
                new BeanPropertyRowMapper<>(Ratings.class)).size();
        int stars3 = jdbcTemplate.query("select * from ratings where numberOfStarsRated = 3",
                new BeanPropertyRowMapper<>(Ratings.class)).size();
        int stars2 = jdbcTemplate.query("select * from ratings where numberOfStarsRated = 2",
                new BeanPropertyRowMapper<>(Ratings.class)).size();
        int stars1 = jdbcTemplate.query("select * from ratings where numberOfStarsRated = 1",
                new BeanPropertyRowMapper<>(Ratings.class)).size();
        return Double.parseDouble(formats.format(
                (5 * stars5 + 4 * stars4 + 3 * stars3 + 2 * stars2 + stars1) / totalResponse));
    }

    private String rateTheApp(long totalResponse) {
        double ratings = calculateRatings(totalResponse);
        if (jdbcTemplate.query("select * from ratingDetails", new BeanPropertyRowMapper<>(RatingDetails.class)).isEmpty())
            jdbcTemplate.update("insert into ratingDetails values (?,?)", totalResponse, ratings);
        jdbcTemplate.update("update ratingDetails set numberOfRatings = ?, currentRatings = ?", totalResponse, ratings);
        return "Thank you for rating!";
    }

    @Override
    public List<HelpAndFAQs> helpAndFAQs() {
        return jdbcTemplate.query("select * from helpAndFAQs",
                new BeanPropertyRowMapper<>(HelpAndFAQs.class));
    }

}
