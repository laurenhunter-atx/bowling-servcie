package bowling

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.annotation.PostConstruct

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles(["integration-test"])
class BaseSpec extends Specification {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    protected WebApplicationContext context

    protected MockMvc mvc

    protected def baseUri() {
        return String.format("http://%s:%d", 'localhost', 8080)
    }

    @PostConstruct
    setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build()
    }
}
