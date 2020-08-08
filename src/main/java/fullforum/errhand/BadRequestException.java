package fullforum.errhand;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 对应400状态码
 */
public class BadRequestException extends ResponseStatusException {
    /**
     * 这个成员对于前端没有用，对于测试来说，可以方便测试
     */
    public ErrorCode code;

    public BadRequestException(ErrorCode code, String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.code = code;
    }
}
