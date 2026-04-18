package org.example.parlop.deputy;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeputyRepository extends CrudRepository<Deputy, Integer> {

    @Query("""
        SELECT * FROM deputy ORDER BY deputy_id
    """)
    List<Deputy> findAllOrdered();

    @Query("""
        SELECT COUNT(*) > 0 FROM deputy WHERE deputy_id = :deputyId
    """)
    boolean existsByDeputyId(Integer deputyId);
}
