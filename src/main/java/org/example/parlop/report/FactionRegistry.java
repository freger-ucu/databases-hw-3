package org.example.parlop.report;

import java.time.LocalDate;

/**
 * Report~1 projection: one row per faction with joined party and
 * government, deputy count as a scalar subquery. Phones are fetched
 * separately (see {@link ReportRepository#findFactionPhones}).
 */
public record FactionRegistry(
        Integer factionRegistrationId,
        String factionName,
        LocalDate foundingDate,
        String ideology,
        String hqCity,
        String hqStreet,
        String hqBuildingNo,
        String partyName,
        String partyRegistrationNumber,
        String governmentName,
        String primeMinisterName,
        long deputyCount
) {}
