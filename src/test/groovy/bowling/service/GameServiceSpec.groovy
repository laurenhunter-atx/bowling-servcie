package bowling.service

import bowling.entity.GameEntity
import bowling.entity.PlayerEntity
import bowling.entity.RollEntity
import bowling.exception.ValidationException
import bowling.repository.GameEntityRepository
import bowling.repository.RollEntityRepository
import spock.lang.Specification
import spock.lang.Unroll

import javax.persistence.EntityNotFoundException

import static bowling.ARandom.aRandom

class GameServiceSpec extends Specification {
    private GameEntityRepository gameRepository
    private RollEntityRepository rollRepository
    private GameService service

    def setup() {
        gameRepository = Mock(GameEntityRepository)
        rollRepository = Mock(RollEntityRepository)
        service = new GameService(gameRepository, rollRepository)
    }

    def "should throw validation exception when no players exist for game"() {
        when:
        service.createGame(GameEntity.builder().build())

        then:
        thrown(ValidationException)
    }

    def "should throw not found exception when no game does not exist"() {
        given:
        gameRepository.findById(*_) >> Optional.empty()

        when:
        service.getGame(aRandom.uuid())

        then:
        thrown(EntityNotFoundException)
    }

    def "should throw validation exception when wrong player for roll"() {
        given:
        PlayerEntity player1 = PlayerEntity.builder().id(aRandom.uuid()).build()
        PlayerEntity player2 = PlayerEntity.builder().id(aRandom.uuid()).build()
        GameEntity game = GameEntity.builder()
                .id(aRandom.uuid())
                .currentPlayerIndex(0)
                .players([player1, player2])
                .build()
        gameRepository.findOneByIdForUpdate(*_) >> Optional.of(game)

        when:
        service.roll(game.id, player2.id, RollEntity.builder().build())

        then:
        thrown(ValidationException)
    }

    @Unroll
    def "should update game state for roll when #desc"() {
        given:
        PlayerEntity player1 = PlayerEntity.builder().id(aRandom.uuid()).build()
        PlayerEntity player2 = PlayerEntity.builder().id(aRandom.uuid()).build()
        GameEntity game = GameEntity.builder()
                .id(aRandom.uuid())
                .frame(frame)
                .currentPlayerIndex(playerIndex)
                .players([player1, player2])
                .build()

        and:
        RollEntity roll = RollEntity.builder()
                .spare(spare)
                .strike(strike)
                .frame(frame)
                .throwForFrame(throwForFrame)
                .build()

        when:
        GameEntity updatedGame = service.updateGameStateForRoll(game, roll, prevRollStrike)

        then:
        assert updatedGame.frame == expectedFrame
        assert updatedGame.currentPlayerIndex == expectedPlayerIndex

        where:
        prevRollStrike | spare | strike | playerIndex | frame | throwForFrame | expectedFrame | expectedPlayerIndex | desc
        false          | false | false  | 0           | 1     | 1             | 1             | 0                   | 'not next turn'
        false          | false | false  | 0           | 1     | 2             | 1             | 1                   | 'is next turn'
        false          | false | false  | 1           | 1     | 2             | 2             | 0                   | 'is next frame'
    }

    @Unroll
    def "should calculate if players turn is over #desc"() {
        given:
        RollEntity roll = RollEntity.builder()
                .spare(spare)
                .strike(strike)
                .frame(frame)
                .throwForFrame(throwForFrame)
                .build()

        expect:
        assert service.isNextPlayersTurn(roll, prevRollIsStrike) == isNext

        where:
        frame | prevRollIsStrike | throwForFrame | strike | spare | isNext | desc
        0     | false            | 1             | false  | false | false  | '1st roll'
        0     | false            | 2             | false  | false | true   | '2nd roll'
        0     | false            | 1             | true   | false | true   | 'roll a strike'
        10    | false            | 1             | false  | false | true   | '10th frame 1st roll'
        10    | false            | 3             | false  | false | true   | '10th frame 3rd roll'
        10    | false            | 2             | false  | false | true   | '10th frame 2nd roll no strike'
        10    | false            | 2             | false  | true  | false  | '10th frame 2nd roll spare'
        10    | false            | 2             | true   | false | false  | '10th frame 2nd roll strike'
        10    | true             | 2             | false  | false | false  | '10th frame 2nd roll & strike'
    }

    def "should calculate next player index"() {
        given:
        PlayerEntity player1 = PlayerEntity.builder().build()
        PlayerEntity player2 = PlayerEntity.builder().build()

        expect:
        assert service.calculateNextPlayer([player1, player2], playerIndex) == expectedPlayerIndex

        where:
        playerIndex | expectedPlayerIndex
        0           | 1
        1           | 0
    }

}
