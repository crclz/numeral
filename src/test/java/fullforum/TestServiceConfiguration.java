package fullforum;

import fullforum.dependency.FakeAuth;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;

@TestConfiguration
public class TestServiceConfiguration {

    @Bean
    @Primary// Configuration一直是生效的，本类只是“重写”了某些东西，所以primary
    @RequestScope
    public FakeAuth constructFakeAuth() {
        return new FakeAuth();
    }
}
