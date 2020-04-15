package bowling.repository;

import bowling.entity.RollEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RollEntityRepository extends JpaRepository<RollEntity, UUID> {
}
