package bowling.service

import bowling.entity.GameEntity
import bowling.exception.ValidationException
import bowling.repository.GameRepository
import spock.lang.Specification

import javax.persistence.EntityNotFoundException

import static bowling.ARandom.aRandom

class GameServiceSpec extends Specification {
    private GameRepository repository
    private GameService service

    def setup() {
        repository = Mock(GameRepository)
        service = new GameService(repository)
    }

    def "should throw validation exception when no players exist for game"() {
        when:
        service.createGame( GameEntity.builder().build())

        then:
        thrown(ValidationException)
    }

    def "should throw not found exception when no game does not exist"() {
        given:
        repository.findById(*_) >> Optional.empty()

        when:
        service.getGame(aRandom.uuid())

        then:
        thrown(EntityNotFoundException)
    }
}
