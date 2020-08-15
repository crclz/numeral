package fullforum.controllers;

import fullforum.data.models.Document;
import fullforum.data.models.Membership;
import fullforum.data.models.Message;
import fullforum.data.models.Team;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateMessageModel;
import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.in.PatchTeamModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QTeam;
import fullforum.errhand.BadRequestException;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
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
@RequestMapping("/api/teams")
@Validated
public class TeamsController {
    @Autowired
    IAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    ModelMapper mapper;

    @Autowired
    EntityManager entityManager;

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


    @PostMapping
    public IdDto createTeam(@RequestBody @Valid CreateTeamModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var team = new Team(snowflake.nextId(), auth.userId(), model.name, model.description);
        teamRepository.save(team);

        var membership = new Membership(snowflake.nextId(), team.getId(), auth.userId());
        membershipRepository.save(membership);

        var message = new Message(snowflake.nextId(), -1L, auth.userId());
        message.setTitle("创建团队通知");
        message.setContent("你已成功创建团队 " + team.getName());
        messageRepository.save(message);

        return new IdDto(team.getId());
    }

    @PostMapping("{id}")
    public void creatTeamInvitation(@PathVariable Long id, @RequestBody @Valid CreateMessageModel model) {
        if(!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            throw new NotFoundException("团队不存在");
        }
        var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), id);
        if (membership == null) {
            throw new ForbidException("操作失败，你不在该团队中");
        }

        var receiver = userRepository.findById(model.receiverId).orElse(null);
        if (receiver == null) {
            throw new NotFoundException("该用户不存在");
        }

        var sender = userRepository.findById(auth.userId()).orElse(null);
        assert sender != null;

        var message = new Message(snowflake.nextId(), auth.userId(), model.receiverId);
        message.setTitle("团队邀请通知");

        message.setContent(sender.getUsername() + " 邀请你加入团队 " + team.getName() + " 点击链接加入: " + model.getLink());

        messageRepository.save(message);

    }

    @PatchMapping("{id}")
    public void patchTeam(@PathVariable Long id, @RequestBody @Valid PatchTeamModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            throw new NotFoundException();
        }
        if (team.getLeaderId() != auth.userId()) {
            throw new ForbidException();
        }

        team.setDescription(model.description == null ? team.getDescription() : model.description);
        team.setName(model.name == null ? team.getName() : model.name);
        team.updatedAtNow();
        teamRepository.save(team);
    }

    @DeleteMapping("{id}")
    public void deleteTeam(@PathVariable Long id) {
        // 解散小组。分为2步骤：
        // 1. 将所有成员的membership删除
        // 2. 将小组实体删除
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            throw new NotFoundException();
        }
        if (team.getLeaderId() != auth.userId()) {
            throw new ForbidException();
        }
        var teamDocuments = documentRepository.findAllByTeamId(id);

        for (Document teamDocument : teamDocuments) {
            teamDocument.setTeamId(null);
            documentRepository.save(teamDocument);
        }

        membershipRepository.deleteAllByTeamId(id);
        teamRepository.deleteById(id);
    }

    @GetMapping("{id}")
    public QTeam getTeamById(@PathVariable Long id) {

        var team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            return null;
        }
        return QTeam.convert(team, mapper);
    }

    @GetMapping
    public List<QTeam> getTeams(
            @RequestParam(required = false) Long leaderId,
            @RequestParam(required = false) String teamNameKeyword
    ) {
        var qTeams = new ArrayList<QTeam>();
        var query = entityManager.createQuery(
                "select t from Team t" +
                        " where (:leaderId is null or t.leaderId = :leaderId)" +
                        " and (:teamNameKeyword is null or t.name like :keywordExpr)")
                .setParameter("leaderId", leaderId)
                .setParameter("teamNameKeyword", teamNameKeyword)
                .setParameter("keywordExpr", "%" + teamNameKeyword + "%");
        var results = query.getResultList();
        for (var result : results) {
            qTeams.add(QTeam.convert((Team) result, mapper));
        }
        return qTeams;
    }








}
