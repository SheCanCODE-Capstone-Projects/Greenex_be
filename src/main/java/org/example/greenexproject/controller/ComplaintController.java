package org.example.greenexproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.ComplaintRequest;
import org.example.greenexproject.model.entity.Complaint;
import org.example.greenexproject.service.ComplaintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citizen/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CITIZEN')")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(
            @RequestBody ComplaintRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(complaintService.createComplaint(request));
    }
}
