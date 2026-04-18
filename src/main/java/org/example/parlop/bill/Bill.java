package org.example.parlop.bill;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("bill")
public class Bill {
    @Id
    @Column("bill_number")
    private String billNumber;

    @Column("bill_title")
    private String billTitle;

    @Column("bill_type")
    private String billType;

    @Column("government_name")
    private String governmentName;

    @Column("submission_date")
    private LocalDate submissionDate;
}
