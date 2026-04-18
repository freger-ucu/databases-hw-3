package org.example.parlop.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Pipelined queries powering the three reports on output documents
 * defined in Step 1. Backed by NamedParameterJdbcTemplate rather
 * than Spring Data JDBC @Query because each result set is a custom
 * projection that is not a persistent aggregate root.
 */
@Repository
public class ReportRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ReportRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------
    // Report 1: Faction Registry Certificate
    // ------------------------------------------------------------------

    private static final String SQL_FACTION_REGISTRY = """
        SELECT  f.faction_registration_id                         AS faction_registration_id,
                f.faction_name                                    AS faction_name,
                f.founding_date                                   AS founding_date,
                f.ideology                                        AS ideology,
                f.hq_city                                         AS hq_city,
                f.hq_street                                       AS hq_street,
                f.hq_building_no                                  AS hq_building_no,
                p.party_name                                      AS party_name,
                p.party_registration_number                       AS party_registration_number,
                f.government_name                                 AS government_name,
                g.prime_minister_name                             AS prime_minister_name,
                (SELECT COUNT(*) FROM deputy d
                  WHERE d.faction_registration_id = f.faction_registration_id)
                                                                  AS deputy_count
          FROM  opposition_faction  f
          LEFT JOIN political_party p ON p.party_registration_number = f.party_registration_number
          LEFT JOIN government      g ON g.government_name           = f.government_name
         WHERE  f.faction_registration_id = :factionId
        """;

    public Optional<FactionRegistry> findFactionRegistry(Integer factionId) {
        try {
            FactionRegistry row = jdbc.queryForObject(
                SQL_FACTION_REGISTRY,
                new MapSqlParameterSource("factionId", factionId),
                (rs, n) -> new FactionRegistry(
                    rs.getInt("faction_registration_id"),
                    rs.getString("faction_name"),
                    rs.getObject("founding_date", LocalDate.class),
                    rs.getString("ideology"),
                    rs.getString("hq_city"),
                    rs.getString("hq_street"),
                    rs.getString("hq_building_no"),
                    rs.getString("party_name"),
                    rs.getString("party_registration_number"),
                    rs.getString("government_name"),
                    rs.getString("prime_minister_name"),
                    rs.getLong("deputy_count")
                )
            );
            return Optional.ofNullable(row);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private static final String SQL_FACTION_PHONES = """
        SELECT  contact_phone
          FROM  faction_phone
         WHERE  faction_registration_id = :factionId
         ORDER BY id
        """;

    public List<String> findFactionPhones(Integer factionId) {
        return jdbc.queryForList(
            SQL_FACTION_PHONES,
            new MapSqlParameterSource("factionId", factionId),
            String.class
        );
    }

    // ------------------------------------------------------------------
    // Report 2: Faction Voting Discipline Report
    // ------------------------------------------------------------------

    private static final String SQL_DISCIPLINE_ROWS = """
        SELECT  d.deputy_id                                       AS deputy_id,
                d.first_name                                      AS first_name,
                d.last_name                                       AS last_name,
                COUNT(*)                                          AS votes_cast,
                100.0 * SUM(CASE WHEN v.vote_choice = 'for'     THEN 1 ELSE 0 END)
                      / COUNT(*)                                  AS pct_for,
                100.0 * SUM(CASE WHEN v.vote_choice = 'against' THEN 1 ELSE 0 END)
                      / COUNT(*)                                  AS pct_against,
                100.0 * SUM(CASE WHEN v.vote_choice = 'abstain' THEN 1 ELSE 0 END)
                      / COUNT(*)                                  AS pct_abstain,
                100.0 * GREATEST(
                            SUM(CASE WHEN v.vote_choice = 'for'     THEN 1 ELSE 0 END),
                            SUM(CASE WHEN v.vote_choice = 'against' THEN 1 ELSE 0 END),
                            SUM(CASE WHEN v.vote_choice = 'abstain' THEN 1 ELSE 0 END)
                        ) / COUNT(*)                              AS discipline
          FROM  votes_on v
          JOIN  deputy   d ON d.deputy_id = v.deputy_id
         WHERE  d.faction_registration_id = :factionId
           AND  v.session_date BETWEEN :dateFrom AND :dateTo
         GROUP BY d.deputy_id, d.first_name, d.last_name
         ORDER BY d.last_name, d.first_name
        """;

    public List<DisciplineRow> findDisciplineRows(Integer factionId,
                                                  LocalDate dateFrom,
                                                  LocalDate dateTo) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("factionId", factionId)
                .addValue("dateFrom", dateFrom)
                .addValue("dateTo", dateTo);
        return jdbc.query(SQL_DISCIPLINE_ROWS, params, (rs, n) -> new DisciplineRow(
            rs.getInt("deputy_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getLong("votes_cast"),
            rs.getDouble("pct_for"),
            rs.getDouble("pct_against"),
            rs.getDouble("pct_abstain"),
            rs.getDouble("discipline")
        ));
    }

    // ------------------------------------------------------------------
    // Report 3: Authorship Digest by Bill Type
    // ------------------------------------------------------------------

    private static final String SQL_AUTHORSHIP_DIGEST = """
        SELECT  b.bill_number                                     AS bill_number,
                b.bill_title                                      AS bill_title,
                CASE b.bill_type
                    WHEN 'DraftLaw'   THEN dl.reading_stage
                    WHEN 'Resolution' THEN r.scope
                    WHEN 'Appeal'     THEN ap.addressee
                END                                               AS subtype_attr,
                COALESCE(
                    GROUP_CONCAT(
                        CONCAT(d.first_name, ' ', d.last_name)
                        ORDER BY d.last_name, d.first_name
                        SEPARATOR ', '
                    ),
                    ''
                )                                                 AS authors,
                b.submission_date                                 AS submission_date
          FROM  bill b
          LEFT JOIN draft_law   dl ON dl.bill_number = b.bill_number
          LEFT JOIN resolution  r  ON r.bill_number  = b.bill_number
          LEFT JOIN appeal      ap ON ap.bill_number = b.bill_number
          LEFT JOIN authors     a  ON a.bill_number  = b.bill_number
          LEFT JOIN deputy      d  ON d.deputy_id    = a.deputy_id
         WHERE  b.bill_type = :billType
         GROUP BY b.bill_number, b.bill_title, b.bill_type,
                  dl.reading_stage, r.scope, ap.addressee, b.submission_date
         ORDER BY b.submission_date, b.bill_number
        """;

    public List<AuthorshipRow> findAuthorshipDigest(String billType) {
        return jdbc.query(
            SQL_AUTHORSHIP_DIGEST,
            Map.of("billType", billType),
            (rs, n) -> new AuthorshipRow(
                rs.getString("bill_number"),
                rs.getString("bill_title"),
                rs.getString("subtype_attr"),
                rs.getString("authors"),
                rs.getObject("submission_date", LocalDate.class)
            )
        );
    }
}
