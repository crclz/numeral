package fullforum.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fullforum.data.models.User;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.LoginModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import javax.servlet.http.Cookie;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


/**
 * AccessController的测试由于涉及到cookie，就没有使用 TestServiceConfiguration
 * 并且，这个测试集合里面，mockMvc被大量使用。其余测试里，mockMvc不作为主要测试手段。
 */
@TestPropertySource("classpath:unittest.properties")
@Rollback
@Transactional
@SpringBootTest
// Do not fake authentication service
//@ContextConfiguration(classes = TestServiceConfiguration.class)
@AutoConfigureMockMvc
public class AccessControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessController accessController;

    @Test
    void me_return_null_when_not_login() throws Exception {
        mockMvc.perform(get("/api/access/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));// "" is null
    }

    @Test
    void me_return_user_info_when_login() throws Exception {
        // Arrange
        var user = new User(1, "u123", "aaaaaa");
        userRepository.save(user);

        // Act

        var usernameCookie = new Cookie("username", user.getUsername());
        var passwordCookie = new Cookie("password", user.getPassword());

        mockMvc.perform(get("/api/access/me").cookie(usernameCookie, passwordCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo(user.getUsername())))
                .andExpect(jsonPath("$.id", hasToString(user.getId().toString())));
        // （上一行）如果不把id转化为字符串来做比较的话，测试框架会认为1L不等于1
    }

    @Test
    void me_clear_cookie_when_wrong_cookie_username_and_password() throws Exception {
        var usernameCookie = new Cookie("username", "asddad");
        var passwordCookie = new Cookie("password", "adasdad");

        mockMvc.perform(get("/api/access/me").cookie(usernameCookie, passwordCookie))
                .andExpect(cookie().value("username", nullValue()))
                .andExpect(cookie().value("password", nullValue()));
    }

    @Autowired
    private ObjectMapper mapper;

    @Test
    void login_return_bad_request_when_username_not_exist() throws Exception {
        var loginModel = new LoginModel("asdas", "asdasdasda");
        mockMvc.perform(post("/api/access/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginModel)))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsStringIgnoringCase("username")));
    }

    @Test
    void login_return_bad_request_when_password_is_wrong() throws Exception {
        var user = new User(1, "aaa", "sd122daas");
        userRepository.save(user);

        var loginModel = new LoginModel("aaa", "asdasdasda");
        mockMvc.perform(post("/api/access/login")
                .content(mapper.writeValueAsString(loginModel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsStringIgnoringCase("password")));
    }

    @Test
    void login_return_ok_and_set_cookie_when_all_ok() throws Exception {
        var user = new User(1, "aaa", "sd122daas");
        userRepository.save(user);

        var loginModel = new LoginModel(user.getUsername(), user.getPassword());
        mockMvc.perform(post("/api/access/login")
                .content(mapper.writeValueAsString(loginModel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().value("username", user.getUsername()))
                .andExpect(cookie().value("password", user.getPassword()));
    }
}
