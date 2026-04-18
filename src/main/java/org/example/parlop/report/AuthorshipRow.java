package org.example.parlop.report;

import java.time.LocalDate;

/**
 * Report~3 projection: one row per bill of the selected type.
 * subtypeAttr is resolved by the controller's SQL via a CASE on
 * bill_type (reading_stage / scope / addressee); subtypeLabel
 * names the corresponding attribute for the rendered header.
 */
public record AuthorshipRow(
        String billNumber,
        String billTitle,
        String subtypeAttr,
        String authors,
        LocalDate submissionDate
) {}
