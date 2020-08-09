package fullforum.controllers;

import fullforum.errhand.BadRequestException;
import fullforum.services.IAuth;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.LoginModel;
import fullforum.dto.out.Quser;
import fullforum.errhand.ErrorCode;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;

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
        response.addCookie(usernameCookie);

        var passwordCookie = new Cookie("password", model.password);
        passwordCookie.setMaxAge(maxage);
        response.addCookie(passwordCookie);
    }
}
