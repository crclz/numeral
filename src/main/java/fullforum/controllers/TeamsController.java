package fullforum.controllers;

import fullforum.data.models.Membership;
import fullforum.data.models.Team;
import fullforum.data.repos.MembershipRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.PatchTeamModel;
import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QTeam;
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
import java.lang.reflect.Member;
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
    MembershipRepository membershipRepository;


    @PostMapping
    public IdDto createTeam(@RequestBody CreateTeamModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var team = new Team(snowflake.nextId(), auth.userId(), model.name, model.description);
        teamRepository.save(team);
        return new IdDto(team.getId());
    }

    @PatchMapping("{id}")
    public void patchTeam(@PathVariable Long id, @RequestBody PatchTeamModel model) {
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
                        " and (:teamNameKeyword is null or t.name like :teamNameKeyword)")
                .setParameter("leaderId", leaderId)
                .setParameter("teamNameKeyword", "%" + teamNameKeyword + "%");
        var results = query.getResultList();
        for (var result:results) {
            qTeams.add(QTeam.convert((Team)result, mapper));
        }
        return qTeams;
    }



}
