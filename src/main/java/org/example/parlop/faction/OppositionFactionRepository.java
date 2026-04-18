package org.example.parlop.faction;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OppositionFactionRepository
        extends CrudRepository<OppositionFaction, Integer> {

    @Query("""
        SELECT faction_registration_id, faction_name
        FROM opposition_faction
        ORDER BY faction_registration_id
    """)
    List<OppositionFaction> findAllOrdered();
}
