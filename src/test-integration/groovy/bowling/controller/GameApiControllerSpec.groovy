package bowling.controller

import bowling.BaseSpec
import bowling.BowlingServiceClient
import bowling.api.Game
import bowling.api.Player
import org.springframework.beans.factory.annotation.Autowired

import static bowling.ARandom.aRandom
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class GameApiControllerSpec extends BaseSpec {

    @Autowired
    BowlingServiceClient client

    def "should create game with players"() {
        given:
        Player player1 = Player.builder().name(aRandom.name().firstName()).build()
        Player player2 = Player.builder().name(aRandom.name().firstName()).build()
        Game game = Game.builder().players([player1, player2]).build()

        and:
        client.createGame(game)

        when:
        Game createdGame = client.responseToClass(client.getGame(game.id), Game.class)

        then:
        assert createdGame.frame == 0
        assert createdGame.currentPlayerId == createdGame.players.get(0).id
        assert createdGame.players.size() == 2
        assert createdGame.players.get(0).score == 0
        assert createdGame.players.get(0).rolls.size() == 0
        assert createdGame.players.get(1).score == 0
        assert createdGame.players.get(1).rolls.size() == 0
    }

    def "should return bad request when game has no players"() {
        when:
        client.createGame(Game.builder().build(), status().isBadRequest())

        then:
        noExceptionThrown()
    }

    def "should return not found when game does not exist"() {
        when:
        client.getGame(aRandom.uuid(), status().isNotFound())

        then:
        noExceptionThrown()
    }
}
