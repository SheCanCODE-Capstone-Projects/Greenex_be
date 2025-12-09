package org.example.greenexproject.controller;

import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.service.WasteCompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/waste/companies")
public class WasteCompanyController {

    private final WasteCompanyService service;

    public WasteCompanyController(WasteCompanyService service) {
        this.service = service;
    }


    @GetMapping("/pending")
    public Page<WasteCompany> getPendingCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "11") int size
    ) {
        return service.getPendingCompanies(PageRequest.of(page, size));
    }

    @PatchMapping("/{companyId}/approve")
    public WasteCompany approveCompany(@PathVariable UUID companyId) {
        return service.approveCompany(companyId);
    }


    @PatchMapping("/{companyId}/reject")
    public WasteCompany rejectCompany(@PathVariable UUID companyId) {
        return service.rejectCompany(companyId);
    }
}
