package fullforum.controllers;

import fullforum.data.repos.UserRepository;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.Quser;
import fullforum.errhand.*;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import fullforum.dto.in.CreateUserModel;
import fullforum.dto.in.PatchUserModel;
import fullforum.data.models.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    Snowflake snowflake;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IAuth auth;


    @PostMapping
    public IdDto createUser(@Valid @RequestBody CreateUserModel model) {
        var userInDb = userRepository.findByUsername(model.username);
        if (userInDb != null) {
            throw new BadRequestException(ErrorCode.UniqueViolation, "Username already exist");
        }

        var user = new User(snowflake.nextId(), model.username, model.password);
        userRepository.save(user);

        return new IdDto(user.getId());
    }

    @PatchMapping("{id}")
    public void patchUser(@Valid @RequestBody PatchUserModel model, @PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new NotFoundException();
        }
        if (user.getId() != auth.userId()) {
            throw new ForbidException();
        }
        // check ok
        user.setPassword(model.password);
        userRepository.save(user);
    }

    @PostMapping("report-error")
    public void reportError(@RequestParam HttpStatus status) {
        switch (status) {
            case BAD_REQUEST:
                // 400
                throw new BadRequestException(ErrorCode.UniqueViolation, "a");
            case UNAUTHORIZED:
                // 401
                throw new UnauthorizedException();
            case FORBIDDEN:
                // 403
                throw new ForbidException();
            case NOT_FOUND:
                // 404
                throw new NotFoundException();
            default:
                throw new IllegalArgumentException("Unexpected status: " + status);
        }
    }

    @Autowired
    ModelMapper modelMapper;

    @GetMapping("{id}")
    public Quser getUserById(@PathVariable long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

        return Quser.convert(user, modelMapper);
    }
}