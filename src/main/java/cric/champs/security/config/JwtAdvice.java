package cric.champs.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAdvice implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String message;
        final Exception exception = new Exception((String) request.getAttribute("exception"));
        if (exception.getMessage()!=null) {
            if (exception.getCause() != null)
                message = exception.getCause() + "\n" + exception.getMessage();
            else
                message = exception.getMessage();

            byte[] body = new ObjectMapper().writeValueAsBytes(Collections.singletonMap("errors", message));
            response.getOutputStream().write(body);
        } else {
            if (authException.getCause() != null)
                message = authException.getCause() + "\n" + authException.getMessage();
            else
                message = authException.getMessage();

            byte[] body = new ObjectMapper().writeValueAsBytes(Collections.singletonMap("errors", message));
            response.getOutputStream().write(body);
        }
    }
}
