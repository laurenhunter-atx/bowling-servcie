package bowling.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Player {
    private UUID id;
    private String name;
    private int score;
    @Builder.Default
    private List<Roll> rolls = new ArrayList<>();
}
