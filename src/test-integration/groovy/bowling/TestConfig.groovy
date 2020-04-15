package bowling

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@Configuration
class TestConfig extends BaseSpec {

    @Bean
    BowlingServiceClient bowlingServiceClient() {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build()
        return new BowlingServiceClient(baseUri(), mvc)
    }
}
