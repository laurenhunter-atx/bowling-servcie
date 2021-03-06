package bowling.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RollEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private PlayerEntity player;

    @Builder.Default
    private boolean strike = false;

    @Builder.Default
    private boolean spare = false;

    @Builder.Default
    private int pins = 0;

    private int frame;
    private int throwForFrame;
}
