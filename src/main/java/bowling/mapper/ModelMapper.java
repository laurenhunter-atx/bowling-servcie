package bowling.mapper;

import bowling.api.Game;
import bowling.api.Player;
import bowling.api.Roll;
import bowling.entity.GameEntity;
import bowling.entity.PlayerEntity;
import bowling.entity.RollEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class ModelMapper {

    @Mapping(target = "currentPlayerIndex", ignore = true)
    public abstract GameEntity toGameEntity(Game game);

    @Mapping(target = "currentPlayerId", ignore = true)
    public abstract Game toGame(GameEntity gameEntity);

    @AfterMapping
    protected void afterToGame(@MappingTarget Game target, GameEntity source) {
        target.setCurrentPlayerId(source.getPlayers().get(source.getCurrentPlayerIndex()).getId());
    }

    @Mapping(target = "game", ignore = true)
    public abstract PlayerEntity toPlayerEntity(Player player);

    @Mapping(target = "score", ignore = true)
    public abstract Player toPlayer(PlayerEntity playerEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "player", ignore = true)
    public abstract RollEntity toRollEntity(Roll roll);

    public abstract Roll toRoll(RollEntity rollEntity);
}

