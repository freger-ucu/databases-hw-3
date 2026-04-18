package org.example.parlop.report;

import java.time.LocalDate;
import java.util.List;
import org.example.parlop.faction.OppositionFactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read-only endpoints rendering the three reports from Step 8.
 * Each report has a parameter-selection form ("/report/xxx") and a
 * result view ("/report/xxx/view") reached via GET so that URLs are
 * bookmarkable and screenshot-friendly.
 */
@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportRepository reportRepo;
    private final OppositionFactionRepository factionRepo;

    public ReportController(ReportRepository reportRepo,
                            OppositionFactionRepository factionRepo) {
        this.reportRepo = reportRepo;
        this.factionRepo = factionRepo;
    }

    // ---------- landing ----------
    @GetMapping({"", "/"})
    public String index() {
        return "report/index";
    }

    // ------------------------------------------------------------------
    // Report 1: Faction Registry Certificate
    // ------------------------------------------------------------------
    @GetMapping("/registry")
    public String registryForm(Model model) {
        model.addAttribute("factions", factionRepo.findAllOrdered());
        return "report/registry_form";
    }

    @GetMapping("/registry/view")
    public String registryView(@RequestParam Integer factionId, Model model) {
        FactionRegistry cert = reportRepo.findFactionRegistry(factionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Faction " + factionId + " not found"));
        List<String> phones = reportRepo.findFactionPhones(factionId);

        model.addAttribute("cert", cert);
        model.addAttribute("phones", phones);
        model.addAttribute("issueDate", LocalDate.now());
        return "report/registry_result";
    }

    // ------------------------------------------------------------------
    // Report 2: Faction Voting Discipline Report
    // ------------------------------------------------------------------
    @GetMapping("/discipline")
    public String disciplineForm(Model model) {
        model.addAttribute("factions", factionRepo.findAllOrdered());
        return "report/discipline_form";
    }

    @GetMapping("/discipline/view")
    public String disciplineView(@RequestParam Integer factionId,
                                 @RequestParam("from") LocalDate dateFrom,
                                 @RequestParam("to")   LocalDate dateTo,
                                 Model model) {
        if (dateFrom.isAfter(dateTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "from-date must be on or before to-date");
        }
        List<DisciplineRow> rows = reportRepo.findDisciplineRows(
            factionId, dateFrom, dateTo);

        // Faction-total row: aggregate totals weighted by per-deputy vote counts.
        long totalVotes = rows.stream().mapToLong(DisciplineRow::votesCast).sum();
        double totalFor = 0, totalAgainst = 0, totalAbstain = 0, totalDiscipline = 0;
        if (totalVotes > 0) {
            for (DisciplineRow r : rows) {
                totalFor        += r.pctFor()        * r.votesCast();
                totalAgainst    += r.pctAgainst()    * r.votesCast();
                totalAbstain    += r.pctAbstain()    * r.votesCast();
                totalDiscipline += r.discipline()    * r.votesCast();
            }
            totalFor        /= totalVotes;
            totalAgainst    /= totalVotes;
            totalAbstain    /= totalVotes;
            totalDiscipline /= totalVotes;
        }
        DisciplineRow factionTotal = new DisciplineRow(
            null, "Faction", "total", totalVotes,
            totalFor, totalAgainst, totalAbstain, totalDiscipline);

        String factionName = factionRepo.findById(factionId)
            .map(f -> f.getFactionName()).orElse("(unknown)");

        model.addAttribute("rows", rows);
        model.addAttribute("factionTotal", factionTotal);
        model.addAttribute("factionId", factionId);
        model.addAttribute("factionName", factionName);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "report/discipline_result";
    }

    // ------------------------------------------------------------------
    // Report 3: Authorship Digest by Bill Type
    // ------------------------------------------------------------------
    @GetMapping("/authorship")
    public String authorshipForm() {
        return "report/authorship_form";
    }

    @GetMapping("/authorship/view")
    public String authorshipView(@RequestParam String billType, Model model) {
        if (!billType.equals("DraftLaw")
         && !billType.equals("Resolution")
         && !billType.equals("Appeal")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Unknown bill_type: " + billType);
        }
        List<AuthorshipRow> rows = reportRepo.findAuthorshipDigest(billType);

        model.addAttribute("rows", rows);
        model.addAttribute("billType", billType);
        model.addAttribute("subtypeLabel", switch (billType) {
            case "DraftLaw"   -> "reading_stage";
            case "Resolution" -> "scope";
            case "Appeal"     -> "addressee";
            default           -> "subtype_attr";
        });
        return "report/authorship_result";
    }
}
