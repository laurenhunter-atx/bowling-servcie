package bowling.controller;

import bowling.api.Game;
import bowling.mapper.ApiModelMapper;
import bowling.mapper.ModelMapper;
import bowling.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameApiController {
    private final GameService gameService;
    private final ModelMapper modelMapper;
    private final ApiModelMapper apiModelMapper;

    @PostMapping("/game")
    ResponseEntity<Game> createGame(@RequestBody Game game) {
        return new ResponseEntity<>(
                apiModelMapper.toGame(gameService.createGame(modelMapper.toGameEntity(game))),
                HttpStatus.CREATED
        );
    }
}
