package fullforum.controllers;

import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.PatchTeamModel;
import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QTeam;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@Validated
public class TeamsController {
    @Autowired
    IAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;


    @PostMapping
    public IdDto createTeam(@RequestBody CreateTeamModel model) {
        throw new NotYetImplementedException();
        
    }

    @PatchMapping("{id}")
    public void patchTeam(@PathVariable Long id, @RequestBody PatchTeamModel model) {
        throw new NotYetImplementedException();
    }

    @DeleteMapping("{id}")
    public void deleteTeam(@PathVariable Long id) {
        // 解散小组。分为2步骤：
        // 1. 将所有成员的membership删除
        // 2. 将小组实体删除
        throw new NotYetImplementedException();
    }

    @GetMapping("{id}")
    public QTeam getTeamById(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }

    @GetMapping
    public List<QTeam> getTeams(
            @RequestParam(required = false) Long leaderId,
            @RequestParam(required = false) String teamNameKeyword
    ) {
        throw new NotYetImplementedException();
    }

}
