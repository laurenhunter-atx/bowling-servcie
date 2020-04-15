package bowling.mapper;

import bowling.api.Game;
import bowling.api.Roll;
import bowling.entity.GameEntity;
import bowling.entity.RollEntity;
import bowling.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GameMapper {
    private final ModelMapper map;

    public GameEntity toGameEntity(Game game) {
        return map.toGameEntity(game);
    }

    public Game toGame(GameEntity gameEntity) {
        Game game = map.toGame(gameEntity);
        game.setPlayers(
                game
                        .getPlayers().stream()
                        .peek(player -> player.setScore(ScoreCalculator.calculate(player.getRolls())))
                        .collect(Collectors.toList())
        );
        return game;
    }

    public Roll toRoll(RollEntity rollEntity) {
        return map.toRoll(rollEntity);
    }

    public RollEntity toRollEntity(Roll roll) {
        return map.toRollEntity(roll);
    }
}
