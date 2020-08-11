package fullforum.dto.out;

import lombok.Data;
import lombok.Getter;

@Data
public class QMembership extends BaseQDto {
    private Long teamId;
    private Long userId;

    private Quser user;
    private QTeam team;
}
