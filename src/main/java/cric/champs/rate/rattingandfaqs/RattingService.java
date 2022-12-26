package cric.champs.rate.rattingandfaqs;


import cric.champs.entity.Users;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.system.SystemInterface;
import cric.champs.rate.model.HelpAndFAQs;
import cric.champs.rate.model.RatingDetails;
import cric.champs.rate.model.Ratings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class RattingService implements RatingsInterface, HelpAndFAQsInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public SuccessResultModel rating(int numberOfStarsRated, String feedback) {
        if (numberOfStarsRated <= 5 && numberOfStarsRated >= 0) {
            Users user = systemInterface.getUserDetailByUserId();
            if (user == null)
                throw new NullPointerException("Please register to rate");
            List<Ratings> ratings = jdbcTemplate.query("select * from ratings where userId = ?",
                    new BeanPropertyRowMapper<>(Ratings.class), user.getUserId());
            if (ratings.isEmpty())
                jdbcTemplate.update("insert into ratings values(?, ?, ?)", systemInterface.getUserId(),
                        numberOfStarsRated, feedback);
            jdbcTemplate.update("update ratings set numberOfStarsRated = ?, feedback = ? where userId = ?",
                    numberOfStarsRated, feedback, user.getUserId());
            rate(ratings.size());
            return new SuccessResultModel("Thank you for Rating!");
        }
        throw new NullPointerException("Please rate between 1 to 5");
    }

    private void rate(long totalResponse) {
        if (jdbcTemplate.query("select * from ratingDetails", new BeanPropertyRowMapper<>(RatingDetails.class)).isEmpty())
            jdbcTemplate.update("insert into ratingDetails values (?,?)",
                    totalResponse, calculateRatings(totalResponse));
        jdbcTemplate.update("update ratingDetails set numberOfRatings = ?, currentRatings = ?",
                totalResponse, calculateRatings(totalResponse));
    }

    public double calculateRatings(double totalResponse) {
        DecimalFormat formats = new DecimalFormat("0.0");
        int stars5 = getStarCount(5);
        int stars4 = getStarCount(4);
        int stars3 = getStarCount(3);
        int stars2 = getStarCount(2);
        int stars1 = getStarCount(1);

        return Double.parseDouble(formats.format(
                (5 * stars5 + 4 * stars4 + 3 * stars3 + 2 * stars2 + stars1) / totalResponse));
    }

    private int getStarCount(int numberOfStars) {
        return jdbcTemplate.query("select * from ratings where numberOfStarsRated = ?",
                new BeanPropertyRowMapper<>(Ratings.class), numberOfStars).size();
    }

    @Override
    public List<HelpAndFAQs> helpAndFAQs() {
        return jdbcTemplate.query("select * from helpAndFAQs", new BeanPropertyRowMapper<>(HelpAndFAQs.class));
    }

}
