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

    def "should throw validation exception when roll not valid"() {
        given:
        PlayerEntity player1 = PlayerEntity.builder().id(aRandom.uuid()).build()
        PlayerEntity player2 = PlayerEntity.builder().id(aRandom.uuid()).build()
        GameEntity game = GameEntity.builder()
                .id(aRandom.uuid())
                .currentPlayerIndex(0)
                .nextMaxRoll(5)
                .frame(1)
                .players([player1, player2])
                .build()
        gameRepository.findOneByIdForUpdate(*_) >> Optional.of(game)

        when:
        service.roll(game.id, player1.id, RollEntity.builder().frame(1).pins(7).build())

        then:
        thrown(ValidationException)
    }

    def "should add roll"() {
        given:
        PlayerEntity player1 = PlayerEntity.builder().id(aRandom.uuid()).build()
        GameEntity game = GameEntity.builder()
                .id(aRandom.uuid())
                .players([player1])
                .currentPlayerIndex(0)
                .frame(1)
                .build()
        gameRepository.findOneByIdForUpdate(*_) >> Optional.of(game)

        and:
        RollEntity roll = RollEntity.builder().frame(1).throwForFrame(1).pins(5).build()

        when:
        service.roll(game.id, player1.id, roll)

        then:
        1 * rollRepository.save(*_) >> { arguments ->
            RollEntity saved = arguments.get(0)
            assert saved.pins == 5
            assert !saved.spare
            assert !saved.strike
            assert saved.throwForFrame == 1
            assert saved.frame == 1
        }

        and:
        player1.setRolls([roll])
        RollEntity roll2 = RollEntity.builder().frame(1).throwForFrame(1).pins(5).build()

        when:
        service.roll(game.id, player1.id, roll2)

        then:
        1 * rollRepository.save(*_) >> { arguments ->
            RollEntity saved = arguments.get(0)
            assert saved.pins == 5
            assert saved.spare
            assert !saved.strike
            assert saved.throwForFrame == 1
            assert saved.frame == 1
        }
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
                .frame(frame)
                .pins(pins)
                .throwForFrame(throwForFrame)
                .build()

        when:
        GameEntity updatedGame = service.updateGameStateForRoll(game, roll, false)

        then:
        assert updatedGame.frame == expectedFrame
        assert updatedGame.currentPlayerIndex == expectedPlayerIndex
        assert updatedGame.nextMaxRoll == nextMaxRoll

        where:
         pins | playerIndex | frame | throwForFrame | expectedFrame | expectedPlayerIndex | nextMaxRoll | gameComplete | desc
         3    | 0           | 1     | 1             | 1             | 0                   | 7           | false        | 'not next turn'
         0    | 0           | 1     | 2             | 1             | 1                   | 10          | false        | 'is next turn'
         9    | 1           | 1     | 2             | 2             | 0                   | 10          | false        | 'is next frame'
         8    | 1           | 10    | 2             | 10            | 0                   | 10          | true         | 'last frame'
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
        10    | false            | 3             | false  | false | true   | '10th frame 3rd roll no strike'
        10    | true             | 3             | false  | false | true   | '10th frame 3rd roll strike'
        10    | false            | 2             | false  | false | true   | '10th frame 2nd roll no strike'
        10    | false            | 2             | false  | true  | false  | '10th frame 2nd roll spare'
        10    | false            | 2             | true   | false | false  | '10th frame 2nd roll strike'
        10    | true             | 2             | false  | false | false  | '10th frame 2nd roll & strike'
        10    | true             | 1             | false  | false | false  | '10th frame 1nd roll & strike'
    }

    @Unroll
    def "should calculate next player index"() {
        given:
        PlayerEntity player1 = PlayerEntity.builder().build()
        PlayerEntity player2 = PlayerEntity.builder().build()
        PlayerEntity player3 = PlayerEntity.builder().build()

        expect:
        assert service.calculateNextPlayer([player1, player2, player3], currentPlayerIndex) == expectedPlayerIndex

        where:
        currentPlayerIndex | expectedPlayerIndex
        0                  | 1
        1                  | 2
        3                  | 0
    }

    @Unroll
    def "should compute if frame is spare"() {
        given:
        RollEntity prevRoll = RollEntity.builder().strike(prevStrike).pins(prevPins).build()
        RollEntity currentRoll = RollEntity.builder().pins(currPins).strike(currStrike).build()

        expect:
        assert service.rollIsSpare(prevRoll, currentRoll) == isSpare

        where:
        prevStrike | prevPins | currStrike | currPins | isSpare
        true       | 10       | true       | 10       | false
        false      | 1        | false      | 1        | false
        false      | 0        | true       | 10       | false
        true       | 10       | false      | 0        | false
        false      | 1        | false      | 9        | true
    }

    @Unroll
    def "should check if roll is valid"() {
        expect:
        assert service.rollIsValid(pins, nextMax) == expected

        where:
        nextMax | pins | expected
        5       | 3    | true
        10      | 10   | true
        10      | 0    | true
        1       | -1   | false
        4       | 7    | false
    }

}
