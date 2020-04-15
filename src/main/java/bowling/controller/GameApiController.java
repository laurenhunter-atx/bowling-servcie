package bowling.controller;

import bowling.api.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameApiController {

    @GetMapping("/hello")
    public String getHello() {
        return "Hello";
    }

    @PostMapping("/game")
    ResponseEntity<Game> createGame(@RequestBody Game game) {
        return null;
    }
}
