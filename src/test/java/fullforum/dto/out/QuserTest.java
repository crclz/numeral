package fullforum.dto.out;

import fullforum.BaseTest;
import fullforum.data.models.User;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试代码是否能够正常工作
 */
class QuserTest extends BaseTest {

    @Autowired
    ModelMapper modelMapper;

    @Test
    void convert_return_null_when_input_null() {
        assertNull(Quser.convert(null, modelMapper));
    }

    @Test
    void convert_simple_test() {
        var user = new User(5, "sdadas", "adsadasdsadsad");
        var q = Quser.convert(user, modelMapper);

        assertEquals(user.getId(), q.id);
        assertEquals(user.getUsername(), q.username);
    }
}