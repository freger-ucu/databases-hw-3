package org.example.parlop.deputy;

import java.time.LocalDate;
import org.example.parlop.district.ElectoralDistrictRepository;
import org.example.parlop.faction.OppositionFactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles the ADD and DELETE forms from Step 7:
 *   POST /deputy/create         - Form 1, Deputy Enrollment Form
 *   POST /deputy/{id}/delete    - Form 3, Mandate Termination Notice
 */
@Controller
public class DeputyController {

    private final DeputyRepository deputyRepo;
    private final OppositionFactionRepository factionRepo;
    private final ElectoralDistrictRepository districtRepo;

    public DeputyController(DeputyRepository deputyRepo,
                            OppositionFactionRepository factionRepo,
                            ElectoralDistrictRepository districtRepo) {
        this.deputyRepo = deputyRepo;
        this.factionRepo = factionRepo;
        this.districtRepo = districtRepo;
    }

    // ---------- listing ----------
    @GetMapping("/deputy")
    public String list(Model model) {
        model.addAttribute("deputies", deputyRepo.findAllOrdered());
        return "deputy/list";
    }

    // ---------- Form 1: ADD (Deputy Enrollment Form) ----------
    @GetMapping("/deputy/create")
    public String createForm(Model model) {
        model.addAttribute("factions", factionRepo.findAllOrdered());
        model.addAttribute("districts", districtRepo.findAllOrdered());
        return "deputy/create";
    }

    @PostMapping("/deputy/create")
    public String create(@RequestParam Integer deputyId,
                         @RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam(required = false) LocalDate birthDate,
                         @RequestParam Integer factionRegistrationId,
                         @RequestParam LocalDate enrollmentDate,
                         @RequestParam Integer electoralDistrictNumber,
                         RedirectAttributes flash) {

        // Domain validation: deputy_id must be unique.
        if (deputyRepo.existsByDeputyId(deputyId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Deputy with id " + deputyId + " already exists");
        }
        // Referential integrity: faction and district must exist.
        if (factionRepo.findById(factionRegistrationId).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unknown faction_registration_id " + factionRegistrationId);
        }
        if (districtRepo.findById(electoralDistrictNumber).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unknown electoral_district_number " + electoralDistrictNumber);
        }

        Deputy d = new Deputy();
        d.setDeputyId(deputyId);
        d.setFirstName(firstName);
        d.setLastName(lastName);
        d.setBirthDate(birthDate);
        d.setFactionRegistrationId(factionRegistrationId);
        d.setEnrollmentDate(enrollmentDate);
        d.setElectoralDistrictNumber(electoralDistrictNumber);

        deputyRepo.save(d);
        flash.addFlashAttribute("successMessage",
            "Deputy " + firstName + " " + lastName + " (id " + deputyId +
            ") enrolled into faction " + factionRegistrationId + ".");
        return "redirect:/deputy";
    }

    // ---------- Form 3: DELETE (Mandate Termination Notice) ----------
    @GetMapping("/deputy/{id}/delete")
    public String deleteForm(@PathVariable("id") Integer id, Model model) {
        Deputy d = deputyRepo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Deputy " + id + " not found"));
        model.addAttribute("deputy", d);
        return "deputy/delete";
    }

    @PostMapping("/deputy/{id}/delete")
    public String delete(@PathVariable("id") Integer id,
                         @RequestParam String terminationReason,
                         @RequestParam LocalDate terminationDate,
                         @RequestParam String registrarRefNo,
                         RedirectAttributes flash) {
        if (!deputyRepo.existsByDeputyId(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Deputy " + id + " not found");
        }
        // DELETE cascades via ON DELETE CASCADE to authors and votes_on.
        deputyRepo.deleteById(id);
        flash.addFlashAttribute("successMessage",
            "Deputy " + id + " mandate terminated (" + terminationReason +
            ", Registrar ref " + registrarRefNo + ", effective " +
            terminationDate + "). Cascaded to authors and votes_on.");
        return "redirect:/deputy";
    }
}
