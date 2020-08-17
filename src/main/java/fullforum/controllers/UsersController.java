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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/users")
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

        var initialAvatarUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597064723297&di=c4d1baacccfaa045cbec77dfe4b8eacd&imgtype=0&src=http%3A%2F%2Fwww.cxyclub.cn%2FUpload%2FImages%2F2012022009%2F8466A231F7D5FFB3.jpg";

        var user = new User(snowflake.nextId(), model.username, model.password, model.description, initialAvatarUrl);
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
            throw new NotFoundException("用户不存在");
        }
        if (user.getId() != auth.userId()) {
            throw new ForbidException();
        }
        // check ok
        if (model.password != null) {
            user.setPassword(model.password);
        }
        if (model.description != null) {
            user.setDescription(model.description);
        }
        if (model.avatarUrl != null) {
            user.setAvatarUrl(model.avatarUrl);
        }
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

    @PersistenceContext
    EntityManager entityManager;

    @GetMapping("find-by-username")
    public Quser getUsers(@RequestParam String username) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return Quser.convert(user, modelMapper);
    }
}