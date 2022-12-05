package cric.champs.service.system;

import cric.champs.entity.Tokens;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenInterface {

    boolean verifyToken(String token);

    String generate();

    void remove(String secureToken, String username);

    void save(String token, String username, LocalDateTime now, LocalDateTime plusMinutes, String toString);

    List<Tokens> getTokenDetail(String token);

}
