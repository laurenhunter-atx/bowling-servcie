package bowling

import bowling.api.Game
import bowling.api.Roll
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class BowlingServiceClient {
    @Autowired
    ObjectMapper objectMapper

    private String baseUri
    private MockMvc mvc

    BowlingServiceClient(String baseUri, MockMvc mvc) {
        this.baseUri = baseUri
        this.mvc = mvc
    }

    public <T> T responseToClass(MockHttpServletResponse response, Class<T> clazz) {
        return objectMapper.readValue(response.getContentAsString(), clazz)
    }


    MockHttpServletResponse createGame(Game game, ResultMatcher expectedStatus = status().isCreated()) {
        return mvc.perform(
                post("${baseUri}/game")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(game)))
                .andDo(print()).andExpect(expectedStatus).andReturn().getResponse()
    }

    MockHttpServletResponse getGame(UUID id, ResultMatcher expectedStatus = status().isOk()) {
        return mvc.perform(
                get("${baseUri}/game/${id}")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON))
                .andDo(print()).andExpect(expectedStatus).andReturn().getResponse()
    }

    MockHttpServletResponse createRoll(UUID gameId, UUID playerId, Roll roll, ResultMatcher expectedStatus = status().isCreated()) {
        return mvc.perform(
                post("${baseUri}/game/${gameId}/player/${playerId}/roll")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roll)))
                .andDo(print()).andExpect(expectedStatus).andReturn().getResponse()
    }
}
