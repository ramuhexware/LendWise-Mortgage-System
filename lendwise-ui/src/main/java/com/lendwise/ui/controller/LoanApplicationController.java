package com.lendwise.ui.controller;

import com.lendwise.ui.service.OrchestrationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for loan application views.
 * Calls SOA orchestration layer via REST/SOAP.
 */
@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {

    private final OrchestrationClient orchestrationClient;

    @GetMapping("/new")
    public String newLoanApplication(Model model) {
        model.addAttribute("pageTitle", "New Loan Application");
        return "loan/application";
    }

    @GetMapping("/{loanId}")
    public String viewLoan(@PathVariable String loanId, Model model) {
        var loan = orchestrationClient.getLoanDetails(loanId);
        model.addAttribute("loan", loan);
        model.addAttribute("pageTitle", "Loan " + loanId);
        return "loan/details";
    }

    @GetMapping("/{loanId}/documents")
    public String loanDocuments(@PathVariable String loanId, Model model) {
        var checklist = orchestrationClient.getDocumentChecklist(loanId);
        model.addAttribute("checklist", checklist);
        model.addAttribute("loanId", loanId);
        model.addAttribute("pageTitle", "Documents - " + loanId);
        return "loan/documents";
    }

    @GetMapping("/{loanId}/underwriting")
    public String loanUnderwriting(@PathVariable String loanId, Model model) {
        var decision = orchestrationClient.getUnderwritingDecision(loanId);
        model.addAttribute("decision", decision);
        model.addAttribute("loanId", loanId);
        model.addAttribute("pageTitle", "Underwriting - " + loanId);
        return "loan/underwriting";
    }

    @GetMapping("/{loanId}/pricing")
    public String loanPricing(@PathVariable String loanId, Model model) {
        var pricing = orchestrationClient.getPricingDetails(loanId);
        model.addAttribute("pricing", pricing);
        model.addAttribute("loanId", loanId);
        model.addAttribute("pageTitle", "Pricing - " + loanId);
        return "loan/pricing";
    }

    @GetMapping("/{loanId}/closing")
    public String loanClosing(@PathVariable String loanId, Model model) {
        var cd = orchestrationClient.getClosingDisclosure(loanId);
        model.addAttribute("closingDisclosure", cd);
        model.addAttribute("loanId", loanId);
        model.addAttribute("pageTitle", "Closing - " + loanId);
        return "loan/closing";
    }

    @PostMapping
    @ResponseBody
    public Object submitLoanApplication(@RequestBody Object applicationData) {
        log.info("Submitting loan application");
        return orchestrationClient.createLoan(applicationData);
    }
}
