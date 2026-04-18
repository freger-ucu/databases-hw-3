package org.example.parlop.deputy;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Row of the deputy relation after the Step 5 3NF decomposition.
 * electoral_district_number is a FK to electoral_district.
 */
@Data
@Table("deputy")
public class Deputy {
    @Id
    @Column("deputy_id")
    private Integer deputyId;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("birth_date")
    private LocalDate birthDate;

    @Column("faction_registration_id")
    private Integer factionRegistrationId;

    @Column("enrollment_date")
    private LocalDate enrollmentDate;

    @Column("electoral_district_number")
    private Integer electoralDistrictNumber;
}
