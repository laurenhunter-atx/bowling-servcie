package bowling.repository;

import bowling.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {
}
