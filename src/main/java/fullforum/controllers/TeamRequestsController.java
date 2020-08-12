package fullforum.controllers;

import fullforum.data.models.Membership;
import fullforum.data.models.Team;
import fullforum.data.models.TeamRequest;
import fullforum.data.repos.MembershipRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.TeamRequestRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.in.CreateTeamRequestModel;
import fullforum.dto.in.PatchTeamRequestModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QTeam;
import fullforum.dto.out.QTeamRequest;
import fullforum.dto.out.Quser;
import fullforum.errhand.*;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/team-requests")
@Validated
public class TeamRequestsController {
    @Autowired
    Snowflake snowflake;

    @Autowired
    IAuth auth;

    @Autowired
    ModelMapper mapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    TeamRequestRepository teamRequestRepository;

    @Autowired
    TeamRepository teamRepository;





    @PostMapping
    public IdDto createTeamRequest(@RequestBody CreateTeamRequestModel model) {
        // 没有membership才允许发送请求
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var membership = membershipRepository.findByUserId(auth.userId());
        var team = teamRepository.findById(model.teamId).orElse(null);
        if (team == null) {
            throw new NotFoundException();
        }

        if (membership == null) {
            var teamRequest = new TeamRequest(snowflake.nextId(), model.teamId, auth.userId());
            teamRequestRepository.save(teamRequest);
            return new IdDto(teamRequest.getId());
        } else {
            throw new ForbidException();
        }
    }

    @PatchMapping("{id}")
    public void patchTeamRequest(@RequestBody PatchTeamRequestModel model, @PathVariable Long id) {
        // 注意，当该Request.isHandled为true时，抛出BadRequest：该请求已经被处理

        // 如果agree，那么涉及到Membership的增加
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var request = teamRequestRepository.findById(id).orElse(null);
        if (request == null) {
            throw new NotFoundException();
        }
        var team = teamRepository.findById(request.getTeamId()).orElse(null);
        if (team == null) {
            throw new NotFoundException();
        }
        if (auth.userId() != team.getLeaderId()) {
            throw new ForbidException();
        }

        request.handle(model.agree);
        if (model.agree) {
            var membership = new Membership(snowflake.nextId(), request.getTeamId(), request.getUserId());
            membershipRepository.save(membership);
        }
        teamRequestRepository.save(request);
    }

    @GetMapping("{id}")
    public QTeamRequest getTeamRequestById(@PathVariable Long id) {
        var teamRequest = teamRequestRepository.findById(id).orElse(null);
        if (teamRequest == null) {
            throw new NotFoundException();
        }

        var qUser = Quser.convert(userRepository.findById(teamRequest.getUserId()).orElse(null), mapper);
        var qTeam = QTeam.convert(teamRepository.findById(teamRequest.getTeamId()).orElse(null), mapper);


        return QTeamRequest.convert(teamRequest, mapper, qTeam, qUser);
    }

    @GetMapping
    public List<QTeamRequest> getTeamRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean isHandled,
            @RequestParam(required = false) Boolean isAgree
    ) {
        var qRequests = new ArrayList<QTeamRequest>();

        var query = entityManager.createQuery(
                "select tr from TeamRequest tr" +
                        " where (:userId is null or tr.userId = :userId)" +
                        " and (:teamId is null or tr.teamId = :teamId)" +
                        " and (:isHandled is null or tr.isHandled = :isHandled)" +
                        " and (:isAgree is null or tr.isAgree = :isAgree)")
                .setParameter("userId", userId)
                .setParameter("teamId", teamId)
                .setParameter("isHandled", isHandled)
                .setParameter("isAgree", isAgree);
        var results = query.getResultList();
        for (var result:results) {
            var teamRequest = (TeamRequest)result;
            var qUser = Quser.convert(userRepository.findById(teamRequest.getUserId()).orElse(null), mapper);
            var qTeam = QTeam.convert(teamRepository.findById(teamRequest.getTeamId()).orElse(null), mapper);

            qRequests.add(QTeamRequest.convert(teamRequest, mapper, qTeam, qUser));
        }
        return qRequests;
    }

}
