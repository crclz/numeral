package fullforum.controllers;

import fullforum.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 由于项目的主要测试手段是调用controller的对应方法，那么就会缺失一点东西。
 * 这个测试类主要来测试这些缺失的东西
 */
@AutoConfigureMockMvc
public class InnerContractTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;


    // 接下来几个测试用例，都是来测试我们位于errhand包里的几个异常类能否被框架“翻译”为想要的返回数据

    @Test
    void badRequest_400() throws Exception {
        mockMvc.perform(
                post(new URI("/api/users/report-error?status=BAD_REQUEST"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("a"));
    }

    @Test
    void unauthorized_401() throws Exception {
        mockMvc.perform(
                post(new URI("/api/users/report-error?status=UNAUTHORIZED"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden_403() throws Exception {
        mockMvc.perform(
                post(new URI("/api/users/report-error?status=FORBIDDEN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void forbidden_404() throws Exception {
        mockMvc.perform(
                post(new URI("/api/users/report-error?status=NOT_FOUND"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试spring是否能够将输入数据转换为long
     */
    @Test
    void input_model_convert_string_to_long_test() throws Exception {
        mockMvc.perform(post("/internal/long-test").contentType(MediaType.APPLICATION_JSON)
                .content("{\"a\":\"9223372036854775807\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("9223372036854775807"));
    }

    @Test
    @Disabled("不管是毫秒时间戳，还是snowflake id（已调整），暂时不会有超出js Number范围（53位）的long")
    void out_dto_not_having_long() throws Exception {
        String packageName = "fullforum.dto.out";
        List<Class<?>> classList = new ArrayList<>();
        URL root = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", "/"));

        // Filter .class files.
        File[] files = new File(root.getFile()).listFiles((dir, name) -> name.endsWith(".class"));

        // Find classes implementing ICommand.
        for (File file : files) {
            String className = file.getName().replaceAll(".class$", "");
            Class<?> cls = Class.forName(packageName + "." + className);
            classList.add(cls);
        }

        for (var c : classList) {
            var cnt = Arrays.stream(c.getFields())
                    .filter(f -> f.getType().equals(Long.class) || f.getType().equals(Long.TYPE))
                    .count();
            if (cnt != 0) {
                var message = String.format(
                        "%s contains Long or long. long will suffer precision loss in javascript.",
                        c.getName());
                throw new Exception(message);
            }
        }
    }
}
