package fullforum.dto.out;

import lombok.Data;
import lombok.Getter;

@Data
public class QTeamRequest extends BaseQDto {
    private Long userId;
    private Long teamId;
    private boolean isHandled;
    private boolean isAgree;

    private Quser sender;
    private QTeam team;
}
