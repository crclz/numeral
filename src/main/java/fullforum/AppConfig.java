package fullforum;

import fullforum.data.repos.UserRepository;
import fullforum.services.Auth;
import fullforum.services.IAuth;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class AppConfig {

    @Bean
    @RequestScope
    public IAuth IAuth(HttpServletRequest request, HttpServletResponse response, UserRepository userRepository) {
        return new Auth(request, response, userRepository);
    }

    @Bean
    public ModelMapper modelMapper() {
        var mapper = new ModelMapper();

        return mapper;
    }

}
