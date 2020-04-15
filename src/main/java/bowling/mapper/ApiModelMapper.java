package bowling.mapper;

import bowling.api.Game;
import bowling.api.Roll;
import bowling.entity.GameEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApiModelMapper {
    private final ModelMapper map;
}
