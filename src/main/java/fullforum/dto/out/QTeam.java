package fullforum.dto.out;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class QTeam {
    private Long leaderId;
    private String name;
    private String description;
}
