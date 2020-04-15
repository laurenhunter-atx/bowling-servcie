package bowling.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"game"})
public class PlayerEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private  String name;

    @ManyToOne
    private GameEntity game;

    @Builder.Default
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RollEntity> rolls = new ArrayList<>();
}
