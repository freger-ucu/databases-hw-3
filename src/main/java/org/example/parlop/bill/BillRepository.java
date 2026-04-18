package org.example.parlop.bill;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BillRepository extends CrudRepository<Bill, String> {

    @Query("""
        SELECT * FROM bill ORDER BY bill_number
    """)
    List<Bill> findAllOrdered();

    @Query("""
        SELECT COUNT(*) > 0 FROM bill WHERE bill_number = :billNumber
    """)
    boolean existsByBillNumber(String billNumber);

    /**
     * Rename the primary key. ON UPDATE CASCADE on the dependent FKs
     * (draft_law, resolution, appeal, amendment, authors, votes_on)
     * propagates the new value automatically at the MySQL level.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE bill
           SET bill_number = :newBillNumber
         WHERE bill_number = :oldBillNumber
    """)
    int renumber(String oldBillNumber, String newBillNumber);
}
