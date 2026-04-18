package org.example.parlop.bill;

import java.time.LocalDate;
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
 * Handles the UPDATE form from Step 7:
 *   POST /bill/{old}/renumber  - Form 2, Bill Renumbering Notice
 */
@Controller
public class BillController {

    private final BillRepository billRepo;

    public BillController(BillRepository billRepo) {
        this.billRepo = billRepo;
    }

    // ---------- listing ----------
    @GetMapping("/bill")
    public String list(Model model) {
        model.addAttribute("bills", billRepo.findAllOrdered());
        return "bill/list";
    }

    // ---------- Form 2: UPDATE (Bill Renumbering Notice) ----------
    @GetMapping("/bill/{number}/renumber")
    public String renumberForm(@PathVariable("number") String oldNumber,
                               Model model) {
        Bill bill = billRepo.findById(oldNumber).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Bill " + oldNumber + " not found"));
        model.addAttribute("bill", bill);
        return "bill/renumber";
    }

    @PostMapping("/bill/{number}/renumber")
    public String renumber(@PathVariable("number") String oldNumber,
                           @RequestParam String newBillNumber,
                           @RequestParam LocalDate effectiveDate,
                           @RequestParam(required = false) String rationale,
                           @RequestParam String registrarRefNo,
                           RedirectAttributes flash) {
        if (!billRepo.existsByBillNumber(oldNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Bill " + oldNumber + " not found");
        }
        if (billRepo.existsByBillNumber(newBillNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Target bill number " + newBillNumber + " already exists");
        }

        // UPDATE on bill.bill_number cascades via ON UPDATE CASCADE
        // to draft_law, resolution, appeal, amendment, authors, votes_on.
        int updated = billRepo.renumber(oldNumber, newBillNumber);

        flash.addFlashAttribute("successMessage",
            "Bill " + oldNumber + " renumbered to " + newBillNumber +
            " (effective " + effectiveDate + ", Registrar ref " +
            registrarRefNo + "). " + updated + " bill row updated; " +
            "cascaded to dependent tables.");
        return "redirect:/bill";
    }
}
