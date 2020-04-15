package bowling.controller;

import bowling.api.Game;
import bowling.api.Roll;
import bowling.mapper.ModelMapper;
import bowling.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GameApiController {
    private final GameService gameService;
    private final ModelMapper modelMapper;

    @PostMapping("/game")
    ResponseEntity<Game> createGame(@RequestBody Game game) {
        return new ResponseEntity<>(
                modelMapper.toGame(gameService.createGame(modelMapper.toGameEntity(game))),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/game/{id}")
    ResponseEntity<Game> getGame(@PathVariable UUID id) {
        return new ResponseEntity<>(
                modelMapper.toGame(gameService.getGame(id)),
                HttpStatus.OK
        );
    }

    @PostMapping("/game/{gameId}/player/{playerId}/roll")
    ResponseEntity<Roll> roll(@PathVariable UUID gameId, @PathVariable UUID playerId, @RequestBody Roll roll) {
        return new ResponseEntity<>(
                modelMapper.toRoll(gameService.roll(gameId, playerId, modelMapper.toRollEntity(roll))),
                HttpStatus.CREATED
        );
    }
}
