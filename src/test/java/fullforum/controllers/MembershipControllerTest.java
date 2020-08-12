package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Membership;
import fullforum.data.models.Team;
import fullforum.data.models.TeamRequest;
import fullforum.data.models.User;
import fullforum.data.repos.MembershipRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dependency.FakeAuth;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MembershipControllerTest extends BaseTest {
    @Autowired
    FakeAuth auth;

    @Autowired
    ModelMapper mapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    MembershipController membershipController;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    //test deleteMembership

    @Test
    void deleteMembership_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> membershipController.deleteMembership(1L));
    }

    @Test
    void deleteMembership_throw_NotFoundException_when_membership_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> membershipController.deleteMembership(1L));
    }

    @Test
    void deleteMembership_throw_ForbidException_when_user_is_creator_of_team() {
        var team = new Team(1L, 2L, "dsad", "adsd");
        teamRepository.save(team);
        var membership = new Membership(2L, 1L, 2L);
        membershipRepository.save(membership);
        auth.setRealUserId(2L);

        assertThrows(ForbidException.class, () -> membershipController.deleteMembership(2L));
    }

    @Test
    void deleteMembership_throw_ForbidException_when_user_is_not_creator_of_team_and_membership() {
        var team = new Team(1L, 2L, "dsad", "adsd");
        teamRepository.save(team);
        var membership = new Membership(2L, 1L, 2L);
        membershipRepository.save(membership);
        auth.setRealUserId(3L);

        assertThrows(ForbidException.class, () -> membershipController.deleteMembership(2L));
    }

    @Test
    void deleteMembership_return_ok_and_update_db_when_all_ok(){
        var team = new Team(1L, 3L, "dsad", "adsd");
        teamRepository.save(team);
        var membership = new Membership(2L, 1L, 2L);
        membershipRepository.save(membership);
        auth.setRealUserId(2L);

        membershipController.deleteMembership(2L);

        var membershipInDb = membershipRepository.findById(2L).orElse(null);

        assertNull(membershipInDb);

    }

    //test getMembershipById
    @Test
    void getMembershipById_throw_NotFoundException_when_membership_is_not_exist() {
        assertThrows(NotFoundException.class, () -> membershipController.getMembershipById(1L));
    }

    @Test
    void getMembershipById_return_membership_info_when_membership_exist() {
        var user = new User(2L, "n13n", "Dasdsad", "sadsadad", "sadasdsad");
        userRepository.save(user);
        var team = new Team(1L, 3L, "dsad", "adsd");
        teamRepository.save(team);
        var membership = new Membership(2L, 1L, 2L);
        membershipRepository.save(membership);

        var qMembership = membershipController.getMembershipById(membership.getId());

        assertEquals(qMembership.getTeamId(), team.getId());
        assertEquals(qMembership.getUserId(), user.getId());
        assertEquals(qMembership.getId(), membership.getId());
    }

    //test getMemberships

    @Test
    void getMemberships_return_list_of_membership_info() {
        var userEntity1 = new User(2L, "n22n", "Dasdsad", "sadsadad", "sadasdsad");
        var userEntity2 = new User(3L, "nn22", "Dasdsad", "sadsadad", "sadasdsad");
        var userEntity3 = new User(4L, "n32n", "Dasdsad", "sadsadad", "sadasdsad");
        userRepository.save(userEntity1);
        userRepository.save(userEntity2);
        userRepository.save(userEntity3);

        var teamEntity1 = new Team(2L, 2L, "dsad", "adsd");
        var teamEntity2 = new Team(3L, 3L, "dsad", "adsd");
        teamRepository.save(teamEntity1);
        teamRepository.save(teamEntity2);

        var membershipEntity1 = new Membership(1L, 2L, 2L);
        var membershipEntity2 = new Membership(2L, 3L, 3L);
        var membershipEntity3 = new Membership(3L, 3L, 4L);
        membershipRepository.save(membershipEntity1);
        membershipRepository.save(membershipEntity2);
        membershipRepository.save(membershipEntity3);

        var memberships = membershipController.getMemberships(3L, null);
        for (var membership : memberships) {
            assertEquals(membership.getTeamId(), 3L);
        }

    }






}
