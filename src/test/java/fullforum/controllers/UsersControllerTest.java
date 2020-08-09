package fullforum.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fullforum.dependency.FakeAuth;
import fullforum.errhand.BadRequestException;
import fullforum.services.Snowflake;
import fullforum.BaseTest;
import fullforum.dto.in.CreateUserModel;
import fullforum.dto.in.PatchUserModel;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.data.models.User;
import fullforum.data.repos.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.annotation.RequestScope;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;


class UsersControllerTest extends BaseTest {

    @Autowired
    UsersController usersController;

    @Autowired
    Snowflake snowflake;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FakeAuth auth;

    @Autowired
    MockMvc mockMvc;

    @Test
    void createUser_throw_Model_when_model_is_invalid() {
        var model = new CreateUserModel();
        model.username = "user";
        model.password = "ad";
        assertThrows(IllegalArgumentException.class, () -> usersController.createUser(model));
    }

    @Test
    void createUser_throw_BadRequestExcepton_when_username_exist() {
        // Arrange: insert a user with username 'user'
        var previousUser = new User(snowflake.nextId(), "user", "aaaaaaaa");
        usersController.userRepository.save(previousUser);

        // Act & Assert
        var model = new CreateUserModel();
        model.username = "user";
        model.password = "adaqdadsada";

        assertThrows(BadRequestException.class, () -> usersController.createUser(model));
    }

    @Test
    void createUser_return_id_and_update_db_when_all_ok() {
        // Act
        var model = new CreateUserModel();
        model.username = "user";
        model.password = "adaqdadsada";
        var idw = usersController.createUser(model);

        // Assert

        assertNotNull(idw);
        var userId = idw.id;

        // Check db
        userRepository.flush();
        var users = userRepository.findAll();
        assertTrue(users.size() > 0);
        var userInDb = userRepository.findById(userId).orElse(null);
        assertNotNull(userInDb);
        Assertions.assertEquals(userId, userInDb.getId());
        Assertions.assertEquals(model.username, userInDb.getUsername());
        Assertions.assertEquals(model.password, userInDb.getPassword());
    }

    @Autowired
    private ObjectMapper mapper;

    @Test
    void patchUser_return_bad_request_when_model_invalid() throws Exception {
        var model = new PatchUserModel("o234a");

        var r = mockMvc.perform(
                patch("/api/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(model)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(r.getResolvedException()).isExactlyInstanceOf(MethodArgumentNotValidException.class);
    }

    @Test
    void patchUser_throw_unauthorized_when_not_login() {
        var model = new PatchUserModel("o234a6");
        assertThrows(UnauthorizedException.class, () -> usersController.patchUser(model, 1));
    }

    @Test
    void patchUser_return_not_found_when_target_user_not_exist() {
        // Arrange
        auth.setRealUserId(1);

        // Act & Assert
        var model = new PatchUserModel("o234a6");
        assertThrows(NotFoundException.class, () -> usersController.patchUser(model, 2));
    }

    @Test
    @RequestScope
    void patchUser_throw_forbidden_when_target_user_is_not_self() {
        // Arrange
        auth.setRealUserId(1);
        var targetUser = new User(2, "asdasda", "asdsadsa");
        userRepository.save(targetUser);

        // Act & Assert
        var model = new PatchUserModel("o234a6");
        assertThrows(ForbidException.class, () -> usersController.patchUser(model, 2));
    }

    @Test
    void patchUser_return_ok_and_changes_db_when_all_ok() {
        // Arrange
        auth.setRealUserId(1);
        var targetUser = new User(1, "asdasda", "asdsadsa");
        userRepository.save(targetUser);

        // Act
        var model = new PatchUserModel("o234a6");
        usersController.patchUser(model, auth.userId());

        // Assert
        var userInDatabase = userRepository.getOne(auth.userId());
        org.assertj.core.api.Assertions.assertThat(userInDatabase.getPassword()).isEqualTo(model.password);
    }

    // region getUserById test

    @Test
    void getUserById_return_null_when_user_not_exist() {
        var user = usersController.getUserById(1);
        assertThat(user).isNull();
    }

    @Test
    void getUserById_return_user_info_when_user_exist() {
        // Arrange
        var userEntity = new User(1, "asda", "sdsdasdasdsa");
        userRepository.save(userEntity);

        // Act
        var userInfo = usersController.getUserById(1);
        assertThat(userInfo.id).isEqualTo(1);
        assertThat(userInfo.username).isEqualTo(userEntity.getUsername());
    }

    // endregion
}