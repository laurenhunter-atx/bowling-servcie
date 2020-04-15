package bowling.mapper;

import bowling.api.Game;
import bowling.entity.GameEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApiModelMapper {
    private final ModelMapper map;

    public Game toGame(GameEntity gameEntity) {
        return map.toGame(gameEntity);
    }
}
