package fullforum.controllers;

import fullforum.data.models.Membership;
import fullforum.data.models.Message;
import fullforum.data.repos.MembershipRepository;
import fullforum.data.repos.MessageRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.out.QMembership;
import fullforum.dto.out.QTeam;
import fullforum.dto.out.Quser;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/api/memberships")
@Validated
public class MembershipController {
    @Autowired
    IAuth auth;

    @Autowired
    ModelMapper mapper;

    @Autowired
    Snowflake snowflake;

    @Autowired
    EntityManager entityManager;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;


    // 没有POST，因为membership由[同意请求]顺带添加

    @DeleteMapping("{id}")
    @ApiOperation("可以由组员自己来删除自己的membership，也可以由组长踢出组员")
    public void deleteMembership(@PathVariable Long id) {
        if(!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var membership = membershipRepository.findById(id).orElse(null);
        if (membership == null) {
            throw new NotFoundException("成员不在团队中");
        }

        var team = teamRepository.findById(membership.getTeamId()).orElse(null);
        assert team != null;//若team 为null则membership也为null

        var message = new Message(snowflake.nextId(), -1L, membership.getUserId());
        if (auth.userId() == membership.getUserId()) {
            if (auth.userId() == team.getLeaderId()) {// 组长不能踢出自己
                throw new ForbidException("操作失败，队长不能退出团队");
            }
            membershipRepository.deleteById(id);
            //自己主动退出团队的通知
            message.setTitle("退出团队通知");
            message.setContent("你已成功退出团队 " + team.getName());
        } else {
            if (auth.userId() != team.getLeaderId()) {
                throw new ForbidException("操作失败，你没有权限");
            }
            membershipRepository.deleteById(id);
            //被踢出团队的通知
            message.setSenderId(team.getLeaderId());
            message.setTitle("踢出团队通知");
            message.setContent("你已被踢出团队 " + team.getName());
        }
        messageRepository.save(message);

    }

    @GetMapping("{id}")
    public QMembership getMembershipById(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var membership = membershipRepository.findById(id).orElse(null);
        if (membership == null) {
            throw new NotFoundException("记录不存在");
        }
        var qUser = Quser.convert(userRepository.findById(membership.getUserId()).orElse(null), mapper);
        var qTeam = QTeam.convert(teamRepository.findById(membership.getTeamId()).orElse(null), mapper);

        return QMembership.convert(membership, mapper, qUser, qTeam);
    }

    @GetMapping
    public List<QMembership> getMemberships(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long userId
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var qMemberships = new ArrayList<QMembership>();
        var query = entityManager.createQuery(
                "select m from Membership m" +
                        " where (:userId is null or m.userId = :userId)" +
                        " and (:teamId is null or m.teamId = :teamId)")
                .setParameter("userId", userId)
                .setParameter("teamId", teamId);
        var results = query.getResultList();
        for (var result:results) {
            var membership = (Membership) result;
            var qUser = Quser.convert(userRepository.findById(membership.getUserId()).orElse(null), mapper);
            var qTeam = QTeam.convert(teamRepository.findById(membership.getTeamId()).orElse(null), mapper);

            qMemberships.add(QMembership.convert(membership, mapper, qUser, qTeam));
        }
        return qMemberships;

    }
}
