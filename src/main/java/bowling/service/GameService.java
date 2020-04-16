package bowling.service;

import bowling.api.Game;
import bowling.entity.GameEntity;
import bowling.entity.PlayerEntity;
import bowling.entity.RollEntity;
import bowling.exception.ValidationException;
import bowling.repository.GameEntityRepository;
import bowling.repository.RollEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GameService {
    private final GameEntityRepository gameRepository;
    private final RollEntityRepository rollRepository;

    @Transactional
    public GameEntity createGame(GameEntity game) {
        validateGame(game);
        game.setFrame(1);
        game.setNextThrowForFrame(1);
        game.setNextMaxRoll(10);
        game.setPlayers(game.getPlayers().stream().peek(player -> player.setGame(game)).collect(Collectors.toList()));
        return gameRepository.save(game);
    }

    public GameEntity getGame(UUID id) {
        return gameRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public RollEntity roll(UUID gameId, UUID playerId, RollEntity roll) {
        GameEntity game = gameRepository.findOneByIdForUpdate(gameId).orElseThrow(EntityNotFoundException::new);
        PlayerEntity player = game.getPlayers().get(game.getCurrentPlayerIndex());
        validateRoll(player, playerId, roll, game);

        List<RollEntity> playerRolls = player.getRolls();
        RollEntity previousRoll = playerRolls.isEmpty() ? RollEntity.builder().build() : playerRolls.get(playerRolls.size() - 1);
        roll.setPlayer(player);
        roll.setStrike(rollIsStrike(roll));
        roll.setSpare(rollIsSpare(previousRoll, roll));

        gameRepository.save(updateGameStateForRoll(game, roll, previousRoll.isStrike()));
        return rollRepository.save(roll);
    }

    private GameEntity updateGameStateForRoll(GameEntity game, RollEntity roll, boolean prevRollIsStrike) {
        if (isNextPlayersTurn(roll, prevRollIsStrike)) {
            if (isNextFrame(game.getPlayers(), game.getCurrentPlayerIndex())) {
                int frame = game.getFrame();
                game.setGameComplete(isLastFrame(frame));
                game.setFrame(nextFrame(frame));
            }
            game.setCurrentPlayerIndex(calculateNextPlayer(game.getPlayers(), game.getCurrentPlayerIndex()));
            game.setNextMaxRoll(10);
            game.setNextThrowForFrame(1);
        } else {
            game.setNextMaxRoll(10 - roll.getPins());
            game.setNextThrowForFrame(roll.getThrowForFrame() + 1);
        }
        return game;
    }

    private boolean isNextFrame(List<PlayerEntity> players, int currentPlayerIndex) {
        return currentPlayerIndex == players.size() - 1;
    }

    private boolean isNextPlayersTurn(RollEntity roll, boolean previousRollIsStrike) {
        int throwForFrame = roll.getThrowForFrame();
        if (isLastFrame(roll.getFrame()) && throwForFrame > 1) {
            return !(throwForFrame == 2 && (previousRollIsStrike || roll.isSpare() || roll.isStrike()));
        }
        return roll.isStrike() || throwForFrame == 2;
    }

    private int calculateNextPlayer(List<PlayerEntity> players, int currentPlayerIndex) {
        return currentPlayerIndex < players.size() - 1  ? currentPlayerIndex + 1 : 0;
    }

    private void validateGame(GameEntity game) {
        if (game.getPlayers() == null) {
            throw new ValidationException("you need to add some players to the game");
        }
    }

    private void validateRoll(PlayerEntity playerEntity, UUID playerId, RollEntity rollEntity, GameEntity game) {
        if (game.isGameComplete()) {
            throw new ValidationException("Game is over. Start new game.");
        }
        if (!playerEntity.getId().equals(playerId)) {
            throw new ValidationException(String.format("wrong turn. it is %s's turn", playerEntity.getName()));
        }
        if (rollEntity.getFrame() != game.getFrame()) {
            throw new ValidationException(String.format("wrong frame. current frame is ", game.getFrame()));
        }
        if(!rollIsValid(rollEntity.getPins(), game.getNextMaxRoll())) {
            throw new ValidationException(
                    String.format("roll not valid. roll needs to be greater than 0 and less than or equal to ", game.getNextMaxRoll())
            );
        }
        if (rollEntity.getThrowForFrame() != game.getNextThrowForFrame()) {
            throw new ValidationException(String.format("wrong throw for frame %d, on throw %d", game.getFrame(), game.getNextThrowForFrame()));
        }
    }

    private boolean rollIsSpare(RollEntity prevRoll, RollEntity currRoll) {
       return prevRoll.getPins() + currRoll.getPins() == 10 && !prevRoll.isStrike() && !currRoll.isStrike();
    }

    private boolean rollIsStrike(RollEntity rollEntity) {
        return rollEntity.getPins() == 10;
    }

    private boolean isLastFrame(int frame) {
        return frame == 10;
    }

    private int nextFrame(int frame) {
        return isLastFrame(frame) ? frame : frame + 1;
    }

    private boolean rollIsValid(int pins, int max) {
        return pins >= 0 && pins <= max;
    }
}
