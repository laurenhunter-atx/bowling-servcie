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
        assert fetchedGame.nextMaxRoll == 10
        assert fetchedGame.nextThrowForFrame == 1
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

    def "should validate roll"() {
        given:
        Player player1 = Player.builder().name(aRandom.name().firstName()).build()
        Game game = Game.builder().nextMaxRoll(5).players([player1]).build()
        Game createdGame = client.responseToClass(client.createGame(game), Game.class)

        when: "wrong player"
        client.createRoll(createdGame.id, aRandom.uuid(), Roll.builder().build(), status().isBadRequest())

        then:
        noExceptionThrown()

        when: "wrong frame"
        Roll wrongFrame = Roll.builder().frame(2).build()
        client.createRoll(createdGame.id, aRandom.uuid(), wrongFrame, status().isBadRequest())

        then:
        noExceptionThrown()

        when: "bad roll"
        Roll invalidPins = Roll.builder().pins(6).build()
        client.createRoll(createdGame.id, aRandom.uuid(), invalidPins, status().isBadRequest())

        then:
        noExceptionThrown()

        when: "wrong throw for frame"
        Roll invalidThrowForFrame = Roll.builder().pins(5).throwForFrame(2).build()
        client.createRoll(createdGame.id, aRandom.uuid(), invalidThrowForFrame, status().isBadRequest())

        then:
        noExceptionThrown()
    }

    def "should advance game on roll"() {
        given:
        Player player1 = Player.builder().name(aRandom.name().firstName()).build()
        Player player2 = Player.builder().name(aRandom.name().firstName()).build()
        Game game = Game.builder().players([player1, player2]).build()
        Game createdGame = client.responseToClass(client.createGame(game), Game.class)
        UUID player1Id = createdGame.getPlayers().get(0).id
        UUID player2Id = createdGame.getPlayers().get(1).id

        and: "P1 frame 1 throw 1"
        Roll roll = Roll.builder().pins(9).frame(1).throwForFrame(1).build()

        when:
        Roll updatedRoll = client.responseToClass(client.createRoll(createdGame.id, createdGame.currentPlayerId, roll), Roll.class)
        Game updatedGame = client.responseToClass(client.getGame(createdGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 1
        assert updatedGame.currentPlayerId == player1Id

        and: "P1 frame 1 throw 2"
        Roll roll2 = Roll.builder().pins(1).frame(1).throwForFrame(2).build()

        when:
        updatedRoll = client.responseToClass(client.createRoll(createdGame.id, createdGame.currentPlayerId, roll2), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert updatedRoll.spare
        assert updatedGame.frame == 1
        assert updatedGame.currentPlayerId == player2Id

        and: "P2 frame 1 throw 1"
        when:
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(1)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 2
        assert updatedGame.currentPlayerId == player1Id

        and: "P1 frame 2 throw 1"
        Roll roll4 = Roll.builder().pins(1).frame(2).throwForFrame(1).build()

        when:
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll4), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 2
        assert updatedGame.currentPlayerId == player1Id

        and: "P1 frame 2 throw 2"
        Roll roll5 = Roll.builder().pins(1).frame(2).throwForFrame(2).build()

        when:
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll5), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 2
        assert updatedGame.currentPlayerId == player2Id

        and: "P2 frame 2 throw 1"
        Roll roll6 = Roll.builder().pins(8).frame(2).throwForFrame(1).build()

        when:
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll6), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 2
        assert updatedGame.currentPlayerId == player2Id

        and: "P2 frame 2 throw 2"
        Roll roll7 = Roll.builder().pins(2).frame(2).throwForFrame(2).build()

        when:
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll7), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert updatedRoll.spare
        assert updatedGame.frame == 3
        assert updatedGame.currentPlayerId == player1Id

        and: "P1 frame 3 throw 1"
        when:
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(3)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 3
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 3 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(3)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 4
        assert updatedGame.currentPlayerId == player1Id

        when:"P1 frame 4 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(4)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 4
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 4 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(4)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 5
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 5 throw 1"
        Roll roll12 = Roll.builder().pins(0).frame(5).throwForFrame(1).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll12), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 5
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 5 throw 2"
        Roll roll13 = Roll.builder().pins(0).frame(5).throwForFrame(2).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll13), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 5
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 6 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(5)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 6
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 6 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(6)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 6
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 6 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(6)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 7
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 7 throw 1"
        Roll roll17 = Roll.builder().pins(5).frame(7).throwForFrame(1).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll17), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 7
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 7 throw 2"
        Roll roll18 = Roll.builder().pins(5).frame(7).throwForFrame(2).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll18), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert updatedRoll.spare
        assert updatedGame.frame == 7
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 7 throw 1"
        Roll roll19 = Roll.builder().pins(1).frame(7).throwForFrame(1).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll19), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 7
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 7 throw 2"
        Roll roll20 = Roll.builder().pins(2).frame(7).throwForFrame(2).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll20), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 8
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 8 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(8)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 8
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 8 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(8)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 9
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 9 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(9)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 9
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 9 throw 1"
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, strike(9)), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 10
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 10 throw 1"
        Roll roll25 = Roll.builder().pins(2).frame(10).throwForFrame(1).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll25), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 10
        assert updatedGame.currentPlayerId == player1Id

        when: "P1 frame 10 throw 2"
        Roll roll26 = Roll.builder().pins(2).frame(10).throwForFrame(2).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll26), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 10
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 10 throw 1"
        Roll roll27 = Roll.builder().pins(9).frame(10).throwForFrame(1).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll27), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 10
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 10 throw 2"
        Roll roll28 = Roll.builder().pins(1).frame(10).throwForFrame(2).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll28), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert updatedRoll.spare
        assert updatedGame.frame == 10
        assert updatedGame.currentPlayerId == player2Id

        when: "P2 frame 10 throw 3"
        Roll roll29 = Roll.builder().pins(5).frame(10).throwForFrame(3).build()
        updatedRoll = client.responseToClass(client.createRoll(updatedGame.id, updatedGame.currentPlayerId, roll29), Roll.class)
        updatedGame = client.responseToClass(client.getGame(updatedGame.id), Game.class)

        then:
        assert !updatedRoll.strike
        assert !updatedRoll.spare
        assert updatedGame.frame == 10
        assert updatedGame.gameComplete
    }

    def strike(int frame) {
        return Roll.builder().pins(10).frame(frame).throwForFrame(1).build()
    }
}
