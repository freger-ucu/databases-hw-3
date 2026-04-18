package org.example.parlop.district;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("electoral_district")
public class ElectoralDistrict {
    @Id
    @Column("district_number")
    private Integer districtNumber;

    @Column("district_name")
    private String districtName;
}
