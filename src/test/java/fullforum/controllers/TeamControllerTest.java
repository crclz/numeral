package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.*;
import fullforum.data.repos.*;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateMessageModel;
import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.in.PatchTeamModel;
import fullforum.dto.out.QTeam;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TeamControllerTest extends BaseTest {
    @Autowired
    FakeAuth auth;

    @Autowired
    TeamsController teamsController;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    MembershipRepository membershipRepository;



    //test creatTeam
    @Test
    void creatTeam_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateTeamModel("haha", "hahaha");
        assertThrows(UnauthorizedException.class, () -> teamsController.createTeam(model));
    }


    @Test
    void creatTeam_return_id_and_update_db_when_all_ok(){
        auth.setRealUserId(1);
        var model = new CreateTeamModel("haha", "hahaha");
        var tid = teamsController.createTeam(model);
        assertNotNull(tid);

        var teamInDb = teamRepository.findById(tid.id).orElse(null);

        assertNotNull(teamInDb);
        assertThat(teamInDb.getDescription()).isEqualTo(model.description);
        assertThat(teamInDb.getLeaderId()).isEqualTo(1);
        assertThat(teamInDb.getName()).isEqualTo(model.name);
    }

    //test createTeamInvitation

    @Test
    void creatTeamInvitation_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateMessageModel(-1L, 3L, "dass", "Dasdsa", "dass");
        assertThrows(UnauthorizedException.class, () -> teamsController.creatTeamInvitation(999L, model));
    }

    @Test
    void creatTeamInvitation_throw_NotFoundException_when_user_or_team_is_not_exist() {
        auth.setRealUserId(1);
        var model = new CreateMessageModel(-1L, 3L, "dass", "Dasdsa", "dass");
        assertThrows(NotFoundException.class, () -> teamsController.creatTeamInvitation(999L, model));

        var team = new Team(999L, 44L, "Dasd", "adas");
        teamRepository.save(team);

        var membership = new Membership(222L, 999L, 1L);
        membershipRepository.save(membership);
        assertThrows(NotFoundException.class, () -> teamsController.creatTeamInvitation(999L, model));


    }

    @Test
    void creatTeamInvitation_throw_ForbidException_when_user_is_in_team() {
        auth.setRealUserId(1);
        var team = new Team(999L, 44L, "Dasd", "adas");
        teamRepository.save(team);
        var model = new CreateMessageModel(-1L, 3L, "dass", "Dasdsa", "dass");
        assertThrows(ForbidException.class, () -> teamsController.creatTeamInvitation(999L, model));
    }

    @Test
    void creatTeamInvitation_return_ok_and_send_message_to_receiver_when_all_ok() {
        auth.setRealUserId(111);

        var team = new Team(999L, 44L, "Dasd", "adas");
        teamRepository.save(team);

        var membership = new Membership(222L, 999L, 111L);
        membershipRepository.save(membership);

        var receiver = new User(333L, "Dasdad", "dsadsadsad", "dasdsa", "Dasdsada");
        var sender = new User(111L, "dasdsa", "Dassad", "Dassa", "DSaa");
        userRepository.save(receiver);
        userRepository.save(sender);

        var model = new CreateMessageModel(-1L, 333L, "dass", "Dasdsa", "dass");
        teamsController.creatTeamInvitation(999L, model);

        var messages = messageRepository.findAllByReceiverId(333L);
        assertThat(messages.size()).isNotZero();

        for (var message : messages) {
            assertEquals(message.getSenderId(), auth.userId());
            assertEquals(message.getTitle(), "团队邀请通知");
        }
    }


    //test patchTeam

    @Test
    void patchTeam_throw_UnauthorizedException_when_user_is_not_login() {
        var patch = new PatchTeamModel("asdas", "Dadsa");
        assertThrows(UnauthorizedException.class, () -> teamsController.patchTeam( 1L, patch));
    }
    @Test
    void patchTeam_throw_ForbidException_when_user_is_not_leader() {
        var team = new Team(1L, 2L, "dsad", "adsd");
        teamRepository.save(team);

        auth.setRealUserId(1);
        var patch = new PatchTeamModel("asdas", "Dadsa");
        assertThrows(ForbidException.class, () -> teamsController.patchTeam(1L, patch));
    }

    @Test
    void patchTeam_throw_NotFoundException_when_team_is_not_exist(){
        auth.setRealUserId(1);
        var patch = new PatchTeamModel("asdas", "Dadsa");
        assertThrows(NotFoundException.class, () -> teamsController.patchTeam(1L, patch));
    }
    @Test
    void patchDocument_return_ok_and_update_db_when_all_ok() {
        var team = new Team(1L, 2L, "dsad", "adsd");
        teamRepository.save(team);

        auth.setRealUserId(2);
        var patch = new PatchTeamModel("asdas", "Dadsa");
        teamsController.patchTeam(team.getId(), patch);

        var teamInDb = teamRepository.findById(team.getId()).orElse(null);

        assertNotNull(teamInDb);
        assertEquals(teamInDb.getName(), patch.name);
        assertEquals(teamInDb.getDescription(), patch.description);

    }
    //test deleteTeam

    @Test
    void deleteTeam_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> teamsController.deleteTeam(2L));
    }

    @Test
    void deleteTeam_throw_NotFoundException_when_team_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> teamsController.deleteTeam(2L));
    }

    @Test
    void deleteTeam_throw_ForbidException_when_user_is_not_leader() {
        auth.setRealUserId(1);
        var team = new Team(1L, 2L, "dasas", "Dasd");
        teamRepository.save(team);
        assertThrows(ForbidException.class, () -> teamsController.deleteTeam(1L));
    }

    @Test
    void deleteTeam_return_ok_and_send_message_to_members_and_update_db_when_all_ok() {
        auth.setRealUserId(2);
        var team = new Team(1L, 2L, "dasas", "Dasd");
        teamRepository.save(team);

        var teamInDb = teamRepository.findById(1L).orElse(null);
        assertThat(teamInDb).isNotNull();

        var document1 = new Document(101L, 11L, "ASda", "Dasdsa", "Dsadsda");
        var document2 = new Document(102L, 12L, "ASda", "Dasdsa", "Dsadsda");
        document1.setTeamId(1L);
        document2.setTeamId(1L);
        documentRepository.save(document1);
        documentRepository.save(document2);

        var membership1 = new Membership(201L, 1L, 11L);
        var membership2 = new Membership(202L, 1L, 12L);
        membershipRepository.save(membership1);
        membershipRepository.save(membership2);

        teamsController.deleteTeam(1L);

        teamInDb = teamRepository.findById(1L).orElse(null);
        assertThat(teamInDb).isNull();

        var messages = messageRepository.findAllByReceiverId(11L);
        assertThat(messages.size() == 1);
        for (Message message : messages) {
            assertEquals(message.getSenderId(), -1L);
            assertEquals(message.getTitle(), "团队解散通知");
        }

        var membershipInDb = membershipRepository.findByUserIdAndTeamId(11L, 1L);
        assertNull(membershipInDb);
    }

    //test getTeamById

    @Test
    void getTeamById_return_null_when_team_not_exist() {
        var team = teamsController.getTeamById(1L);
        assertThat(team).isNull();
    }

    @Test
    void getTeamById_return_team_info_when_team_exist() {
        var team = new Team(1L, 2L, "haha", "sss");
        teamRepository.save(team);

        var qTeam = teamsController.getTeamById(1L);

        assertThat(qTeam.getLeaderId()).isEqualTo(2);
        assertThat(qTeam.getDescription()).isEqualTo(team.getDescription());
        assertThat(qTeam.getName()).isEqualTo(team.getName());
    }

    //test getTeams
    @Test

    void getTeams_return_list_of_team_infos() {

        var teamEntity1 = new Team(1L, 2L, "awsaf", "sss");
        var teamEntity2 = new Team(2L, 3L, "oiebbbwur", "sss");
        var teamEntity3 = new Team(3L, 3L, "mmks", "sss");
        var teamEntity4 = new Team(4L, 5L, "owieur", "sss");
        var teamEntity5 = new Team(5L, 3L, "bbbss", "sss");

        teamRepository.save(teamEntity1);
        teamRepository.save(teamEntity2);
        teamRepository.save(teamEntity3);
        teamRepository.save(teamEntity4);
        teamRepository.save(teamEntity5);

        var leaderId = 3L;
        var nameKeyWords = "bbb";
        List<QTeam> teamList1 = teamsController.getTeams(leaderId, nameKeyWords);
        for (QTeam qTeam : teamList1) {
            assertEquals(qTeam.getLeaderId(), leaderId);
            assertTrue(qTeam.getName().contains(nameKeyWords));
        }

    }
}
