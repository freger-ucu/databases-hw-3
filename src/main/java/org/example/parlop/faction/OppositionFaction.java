package org.example.parlop.faction;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("opposition_faction")
public class OppositionFaction {
    @Id
    @Column("faction_registration_id")
    private Integer factionRegistrationId;

    @Column("faction_name")
    private String factionName;
}
