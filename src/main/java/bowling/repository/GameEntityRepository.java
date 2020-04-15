package bowling.repository;

import bowling.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GameEntityRepository extends JpaRepository<GameEntity, UUID> {

    @Query(value = "SELECT * FROM game_entity WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<GameEntity> findOneByIdForUpdate(@Param("id") UUID id);
}
