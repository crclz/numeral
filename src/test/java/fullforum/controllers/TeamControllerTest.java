package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Access;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateCommentModel;
import fullforum.dto.in.CreateTeamModel;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TeamControllerTest extends BaseTest {

    @Autowired
    TeamsController teamsController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    FakeAuth auth;


    //test creatTeam

    @Test
    void creatTeam_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateTeamModel("haha", "hahaha");
        assertThrows(UnauthorizedException.class, () -> teamsController.createTeam(model));
    }




}
