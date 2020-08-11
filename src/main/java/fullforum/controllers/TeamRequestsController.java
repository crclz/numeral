package fullforum.controllers;

import fullforum.dto.in.CreateTeamModel;
import fullforum.dto.in.CreateTeamRequestModel;
import fullforum.dto.in.PatchTeamRequestModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QTeamRequest;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/team-requests")
@Validated
public class TeamRequestsController {

    @PostMapping
    public IdDto createTeamRequest(@RequestBody CreateTeamRequestModel model) {
        // 没有membership才允许发送请求

        throw new NotYetImplementedException();
    }

    @PatchMapping("{id}")
    public void patchTeamRequest(@RequestBody PatchTeamRequestModel model, @PathVariable Long id) {
        // 注意，当该Request.isHandled为true时，抛出BadRequest：该请求已经被处理

        // 如果agree，那么涉及到Membership的增加

        throw new NotYetImplementedException();
    }

    @GetMapping("{id}")
    public QTeamRequest getTeamRequestById(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }

    @GetMapping
    public List<QTeamRequest> getTeamRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean isHandled,
            @RequestParam(required = false) Boolean isAgree
    ) {
        throw new NotYetImplementedException();
    }

}
