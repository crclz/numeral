package fullforum.controllers;

import fullforum.data.models.Membership;
import fullforum.dto.out.QMembership;
import io.swagger.annotations.ApiOperation;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/memberships")
@Validated
public class MembershipController {
    // 没有POST，因为membership由[同意请求]顺带添加

    @DeleteMapping("{id}")
    @ApiOperation("可以由组员自己来删除自己的membership，也可以由组长踢出组员")
    public void deleteMembership(@PathVariable Long id) {
        // 组长不能踢出自己

        throw new NotYetImplementedException();
    }

    @GetMapping("{id}")
    public QMembership getMembershipById(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }

    @GetMapping
    public List<Membership> getMemberships(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long userId
    ) {
        throw new NotYetImplementedException();
    }
}
