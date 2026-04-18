package org.example.parlop.report;

/**
 * Report~2 projection: one row per deputy in the selected faction,
 * aggregated over the session-date range. Discipline is reported as
 * the concentration proxy MAX(pct_for, pct_against, pct_abstain): a
 * deputy who always votes the same choice scores 100; a deputy split
 * evenly three ways scores 33.3. This keeps the computation to a
 * single GROUP BY pass without needing a faction-majority CTE.
 */
public record DisciplineRow(
        Integer deputyId,
        String firstName,
        String lastName,
        long votesCast,
        double pctFor,
        double pctAgainst,
        double pctAbstain,
        double discipline
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
