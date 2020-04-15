package bowling.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApiModelMapper {
    private final ModelMapper map;
}
