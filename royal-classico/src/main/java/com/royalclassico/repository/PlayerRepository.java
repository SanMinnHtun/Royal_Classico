package com.royalclassico.repository;

import com.royalclassico.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data MongoDB repository for Player documents.
 */
@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {

    /** Find players that contain a given position string in their positions list. */
    List<Player> findByPositionsContaining(String position);
}
