package fullforum.dto.out;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchUploadResult {
    private int error = 0;
    private List<String> data;
}
