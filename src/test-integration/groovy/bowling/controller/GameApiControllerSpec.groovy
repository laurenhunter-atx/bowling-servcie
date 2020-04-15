package bowling.controller

import bowling.BaseSpec
import bowling.BowlingServiceClient
import bowling.api.Game
import bowling.api.Player
import bowling.api.Roll
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
        Roll roll = Roll.builder().pins(aRandom.intBetween(0, 9)).frame(1).throwForFrame(1).build()

        when:
        client.createRoll(createdGame.id, createdGame.currentPlayerId, roll)
        Game updatedGame = client.responseToClass(client.getGame(createdGame.id), Game.class)

        then:
        assert updatedGame.frame == 1
        assert updatedGame.currentPlayerId == updatedGame.getPlayers().get(0).id
        assert updatedGame.getPlayers().get(0).rolls == [roll]
        assert updatedGame.getPlayers().get(0).score == roll.pins

        and:
        Roll roll2 = Roll.builder().pins(10 - roll.pins).frame(1).throwForFrame(2).build()

        when:
        client.createRoll(createdGame.id, createdGame.currentPlayerId, roll2)
        updatedGame = client.responseToClass(client.getGame(createdGame.id), Game.class)

        then:
        assert updatedGame.frame == 1
        assert updatedGame.currentPlayerId == updatedGame.getPlayers().get(2).id
        assert updatedGame.getPlayers().get(0).rolls == [roll, roll2]
        assert updatedGame.getPlayers().get(0).score == roll.pins + roll2.pins
    }
}
