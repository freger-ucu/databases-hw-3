package org.example.parlop.district;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectoralDistrictRepository
        extends CrudRepository<ElectoralDistrict, Integer> {

    @Query("""
        SELECT district_number, district_name
        FROM electoral_district
        ORDER BY district_number
    """)
    List<ElectoralDistrict> findAllOrdered();
}
