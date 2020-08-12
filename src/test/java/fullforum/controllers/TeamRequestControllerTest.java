package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Membership;
import fullforum.data.models.Team;
import fullforum.data.models.TeamRequest;
import fullforum.data.models.User;
import fullforum.data.repos.*;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.in.CreateTeamRequestModel;
import fullforum.dto.in.PatchTeamModel;
import fullforum.dto.in.PatchTeamRequestModel;
import fullforum.dto.out.QTeam;
import fullforum.dto.out.QTeamRequest;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamRequestControllerTest extends BaseTest {
    @Autowired
    TeamRequestsController teamRequestsController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRequestRepository teamRequestRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    FakeAuth auth;


    //test creatTeamRequest
    @Test
    void creatTeamRequest_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateTeamRequestModel(1L);
        assertThrows(UnauthorizedException.class, () -> teamRequestsController.createTeamRequest(model));
    }
    @Test
    void creatTeamRequest_throw_NotFoundException_when_team_is_not_exist() {
        auth.setRealUserId(1);
        var model = new CreateTeamRequestModel(1L);
        assertThrows(NotFoundException.class, () -> teamRequestsController.createTeamRequest(model));
    }


    @Test
    void creatTeamRequest_return_id_and_update_db_when_all_ok(){
        auth.setRealUserId(1);

        var model = new CreateTeamRequestModel(1L);
        var team = new Team(1L, 2L, "Sss", "sss");
        teamRepository.save(team);

        var id = teamRequestsController.createTeamRequest(model);

        var requestInDb = teamRequestRepository.findById(id.id).orElse(null);

        assertNotNull(requestInDb);
        assertThat(requestInDb.getTeamId()).isEqualTo(model.teamId);
        assertThat(requestInDb.getUserId()).isEqualTo(1);
    }

    //test patchTeam


    @Test
    void patchTeamRequest_throw_UnauthorizedException_when_user_is_not_login() {
        var patch = new PatchTeamRequestModel(true);
        assertThrows(UnauthorizedException.class, () -> teamRequestsController.patchTeamRequest(patch, 1L));
    }
    @Test
    void patchTeam_throw_ForbidException_when_user_is_not_leader() {
        var team = new Team(1L, 2L, "dsad", "adsd");
        teamRepository.save(team);
        var request = new TeamRequest(5L, 3L, 1L);
        teamRequestRepository.save(request);

        auth.setRealUserId(1);
        var patch = new PatchTeamRequestModel(true);
        assertThrows(ForbidException.class, () -> teamRequestsController.patchTeamRequest(patch, 5L));
    }

    @Test
    void patchTeam_throw_NotFoundException_when_team_is_not_exist(){
        auth.setRealUserId(1);
        var patch = new PatchTeamRequestModel(true);
        assertThrows(NotFoundException.class, () -> teamRequestsController.patchTeamRequest(patch, 1L));
    }
    @Test
    void patchTeamRequest_return_ok_and_update_db_when_all_ok() {
        var team = new Team(1L, 2L, "dsad", "adsd");
        teamRepository.save(team);
        var request = new TeamRequest(5L, 3L, 1L);
        teamRequestRepository.save(request);

        auth.setRealUserId(2);
        var patch = new PatchTeamRequestModel(true);

        teamRequestsController.patchTeamRequest(patch, 5L);
        var requestInDb = teamRequestRepository.findById(request.getId()).orElse(null);

        var membershipInDb = membershipRepository.findByUserId(request.getUserId());

        assertNotNull(requestInDb);
        assertNotNull(membershipInDb);
        assertEquals(requestInDb.getTeamId(), team.getId());
        assertTrue(requestInDb.isAgree());
        assertTrue(requestInDb.isHandled());
        assertEquals(membershipInDb.getTeamId(), team.getId());

    }



    //test getTeamRequestById

    @Test
    void getTeamRequestById_throw_NotFoundException_when_request_not_exist() {
        assertThrows(NotFoundException.class, () -> teamRequestsController.getTeamRequestById(1L));
    }

    @Test
    void getTeamRequestById_return_teamRequest_info_when_request_exist() {
        var request = new TeamRequest(5L, 3L, 1L);
        var team = new Team(1L, 2L, "sss", "Sss");
        teamRepository.save(team);
        teamRequestRepository.save(request);

        var qRequest = teamRequestsController.getTeamRequestById(5L);

        assertThat(qRequest.getTeamId()).isEqualTo(1L);
        assertThat(qRequest.getUserId()).isEqualTo(3L);
    }

//    //test getTeamRequests
    @Test

    void getTeamRequestss_return_list_of_request_infos() {
        auth.setRealUserId(1);
        var requestEntity1 = new TeamRequest(1L, 2L, 3L);
        var requestEntity2 = new TeamRequest(2L, 3L, 3L);
        var requestEntity3 = new TeamRequest(3L, 4L, 4L);
        var requestEntity4 = new TeamRequest(4L, 5L, 3L);
        var requestEntity5 = new TeamRequest(5L, 6L, 5L);

        var teamEntity1 = new Team(3L, 9L, "sss", "Sss");
        var teamEntity2 = new Team(4L, 8L, "sss", "Sss");
        var teamEntity3 = new Team(5L, 7L, "sss", "Sss");

        var userEntity1 = new User(2L, "Sdad", "Dasdsada", "Dasd", "Dasdas");
        userRepository.save(userEntity1);

        teamRepository.save(teamEntity1);
        teamRepository.save(teamEntity2);
        teamRepository.save(teamEntity3);


        teamRequestRepository.save(requestEntity1);
        teamRequestRepository.save(requestEntity2);
        teamRequestRepository.save(requestEntity3);
        teamRequestRepository.save(requestEntity4);
        teamRequestRepository.save(requestEntity5);

        var userId = 2L;
        var teamId = 3L;

        List<QTeamRequest> requests = teamRequestsController.getTeamRequests(userId, teamId, null, null);

        for (QTeamRequest qRequest : requests) {
            assertEquals(qRequest.getTeamId(), teamId);
            assertEquals(qRequest.getUserId(), userId);
        }

    }


}
