package fullforum.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fullforum.data.models.User;
import fullforum.errhand.BadRequestException;
import fullforum.services.IAuth;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.LoginModel;
import fullforum.dto.out.Quser;
import fullforum.errhand.ErrorCode;
import org.hibernate.cfg.NotYetImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.Duration;

@Transactional
@RestController
@RequestMapping("/api/access")
public class AccessController {

    @Autowired
    IAuth auth;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping("me")
    public Quser me() {
        if (!auth.isLoggedIn()) {
            return null;
        }

        var user = userRepository.findById(auth.userId()).orElseThrow();

        return Quser.convert(user, modelMapper);
    }

    @PostMapping("login")
    public void login(@RequestBody @Valid LoginModel model, HttpServletResponse response) {
        if (response == null) {
            throw new NullPointerException();
        }
        var user = userRepository.findByUsername(model.username);
        if (user == null) {
            throw new BadRequestException(ErrorCode.UsernameNotExist, "Username not exist");
        }
        if (!user.checkPassword(model.password)) {
            throw new BadRequestException(ErrorCode.WrongPassword, "Password incorrect");
        }

        // username password ok
        // set cookie
        int maxage = (int) Duration.ofDays(180).toSeconds();
        var usernameCookie = new Cookie("username", model.username);
        usernameCookie.setMaxAge(maxage);
        usernameCookie.setPath("/");
        response.addCookie(usernameCookie);

        var passwordCookie = new Cookie("password", model.password);
        passwordCookie.setMaxAge(maxage);
        passwordCookie.setPath("/");
        response.addCookie(passwordCookie);
    }

    @PostMapping("logout")
    public void logout(HttpServletResponse response) {
        var cookie1 = new Cookie("username", null);
        cookie1.setPath("/");
        cookie1.setMaxAge(0);

        var cookie2 = new Cookie("password", null);
        cookie2.setPath("/");
        cookie1.setMaxAge(0);

        response.addCookie(cookie1);
        response.addCookie(cookie2);
    }
}
