package fullforum.controllers;

import fullforum.data.models.Membership;
import fullforum.data.models.Message;
import fullforum.data.models.Team;
import fullforum.data.models.TeamRequest;
import fullforum.data.repos.*;
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
import javax.validation.Valid;
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
    MessageRepository messageRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    TeamRequestRepository teamRequestRepository;

    @Autowired
    TeamRepository teamRepository;


    @PostMapping
    public IdDto createTeamRequest(@RequestBody @Valid CreateTeamRequestModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), model.teamId);

        // 没有membership才允许发送请求
        if (membership != null) {
            throw new ForbidException("你已经在该团队中了");
        }

        // 没有已经存在[且未处理]request，才能发送
        var existingRequest = teamRequestRepository.findByUserIdAndTeamIdAndIsHandled(auth.userId(), model.teamId, false);
        if (existingRequest != null) {
            throw new BadRequestException(ErrorCode.UniqueViolation, "你已经发送过请求，且请求未被处理");
        }

        var team = teamRepository.findById(model.teamId).orElse(null);
        if (team == null) {
            throw new NotFoundException("团队不存在");
        }

        var teamRequest = new TeamRequest(snowflake.nextId(), auth.userId(), model.teamId);
        teamRequestRepository.save(teamRequest);

        // 给组长发送消息通知
        var message = new Message(snowflake.nextId(), -1L, team.getLeaderId());
        message.setTitle("有新的团队申请");
        message.setContent("你的小组 " + team.getName() + " 有新的成员申请");
        message.setLink("/team/" + team.getId());
        messageRepository.save(message);

        return new IdDto(teamRequest.getId());
    }

    @PatchMapping("{id}")
    public void patchTeamRequest(@RequestBody @Valid PatchTeamRequestModel model, @PathVariable Long id) {
        // 注意，当该Request.isHandled为true时，抛出BadRequest：该请求已经被处理

        // 如果agree，那么涉及到Membership的增加
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var request = teamRequestRepository.findById(id).orElse(null);
        if (request == null) {
            throw new NotFoundException("请求不存在");
        }
        if (request.isHandled()) {
            throw new BadRequestException(ErrorCode.InvalidOperation, "请求已经被处理过");
        }
        var team = teamRepository.findById(request.getTeamId()).orElse(null);
        if (team == null) {
            throw new NotFoundException("团队不存在");
        }
        if (auth.userId() != team.getLeaderId()) {
            throw new ForbidException("操作失败，你没有权限");
        }

        request.handle(model.agree);
        if (model.agree) {
            var membership = new Membership(snowflake.nextId(), request.getTeamId(), request.getUserId());
            var message = new Message(snowflake.nextId(), -1L, membership.getUserId());
            message.setTitle("加入团队通知");
            message.setContent("你已成功加入团队 " + team.getName());
            membershipRepository.save(membership);
            messageRepository.save(message);
        }
        teamRequestRepository.save(request);
    }

    @GetMapping("{id}")
    public QTeamRequest getTeamRequestById(@PathVariable Long id) {
        var teamRequest = teamRequestRepository.findById(id).orElse(null);
        if (teamRequest == null) {
            throw new NotFoundException("请求不存在");
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
        for (var result : results) {
            var teamRequest = (TeamRequest) result;
            var qUser = Quser.convert(userRepository.findById(teamRequest.getUserId()).orElse(null), mapper);
            var qTeam = QTeam.convert(teamRepository.findById(teamRequest.getTeamId()).orElse(null), mapper);

            qRequests.add(QTeamRequest.convert(teamRequest, mapper, qTeam, qUser));
        }
        return qRequests;
    }

}
