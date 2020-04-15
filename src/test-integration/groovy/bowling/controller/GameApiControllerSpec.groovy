package bowling.controller

import bowling.BaseSpec
import bowling.BowlingServiceClient
import bowling.api.Game
import bowling.api.Player
import bowling.api.Roll
import bowling.entity.RollEntity
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
        Game createdGame = client.responseToClass(client.createGame(game), Game.class)

        when:
        Game fetchedGame = client.responseToClass(client.getGame(createdGame.id), Game.class)

        then:
        assert fetchedGame.frame == 1
        assert fetchedGame.currentPlayerId == fetchedGame.players.get(0).id
        assert fetchedGame.players.size() == 2
        assert fetchedGame.players.get(0).score == 0
        assert fetchedGame.players.get(0).rolls.size() == 0
        assert fetchedGame.players.get(1).score == 0
        assert fetchedGame.players.get(1).rolls.size() == 0
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

    def "should roll for player"() {
        given:
        Player player1 = Player.builder().name(aRandom.name().firstName()).build()
        Player player2 = Player.builder().name(aRandom.name().firstName()).build()
        Game game = Game.builder().players([player1, player2]).build()
        Game createdGame = client.responseToClass(client.createGame(game), Game.class)

        and:
        Roll roll = Roll.builder().pins(aRandom.intBetween(0, 10)).frame(1).throwForFrame(1).build()

        when:
        Roll createdRoll = client.responseToClass(client.createRoll(createdGame.id, createdGame.currentPlayerId, roll), Roll.class)

        then:
        assert createdRoll.frame == 1
        assert createdRoll.throwForFrame == 1
    }
}
