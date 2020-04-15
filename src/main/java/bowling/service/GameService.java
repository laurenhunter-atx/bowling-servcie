package bowling.service;

import bowling.api.Roll;
import bowling.entity.GameEntity;
import bowling.entity.RollEntity;
import bowling.exception.ValidationException;
import bowling.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GameService {
    private final GameRepository repository;

    @Transactional
    public GameEntity createGame(GameEntity game) {
        validateGame(game);
        game.setFrame(1);
        game.setPlayers(game.getPlayers().stream().peek(player -> player.setGame(game)).collect(Collectors.toList()));
        return repository.save(game);
    }

    public GameEntity getGame(UUID id) {
        return repository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public RollEntity createRoll(UUID gameId, UUID playerId, RollEntity rollEntity) {
        return null;
    }

    private void validateGame(GameEntity game) {
        if (game.getPlayers() == null) {
            throw new ValidationException("you need to add some players to the game");
        }
    }
}
