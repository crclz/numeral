package fullforum.dto.out;

import fullforum.data.models.TeamRequest;
import lombok.Data;
import lombok.Getter;
import org.modelmapper.ModelMapper;

@Data
public class QTeamRequest extends BaseQDto {
    private Long userId;
    private Long teamId;
    private boolean isHandled;
    private boolean isAgree;

    private Quser sender;
    private QTeam team;

    public static QTeamRequest convert(TeamRequest request, ModelMapper mapper, QTeam qteam, Quser quser) {
        var qRequest = mapper.map(request, QTeamRequest.class);
        qRequest.team = qteam;
        qRequest.sender = quser;
        return qRequest;

    }
}
