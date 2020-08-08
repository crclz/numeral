package fullforum.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/internal")
public class ZController {

    public static class LongModel {
        public long a;
    }

    @PostMapping("long-test")
    public long longTest(@Valid @RequestBody LongModel model) {
        return model.a;
    }
}
