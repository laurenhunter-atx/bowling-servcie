package bowling.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Game {
    private UUID id;
    private int frame;
    private UUID currentPlayerId;
    private boolean isGameComplete;
    private List<Player> players;
}
