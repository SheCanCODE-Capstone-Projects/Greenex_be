package org.example.greenexproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.ContactRequest;
import org.example.greenexproject.dto.response.ContactResponse;
import org.example.greenexproject.service.ContactService;
import org.example.greenexproject.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // Public endpoint: anyone can submit a contact message
    @PostMapping("/contact")
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.createContact(request);
        return ResponseEntity.ok(response);
    }

    // Admin endpoints (secured)
    @GetMapping("/contact")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactResponse>> listAll() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @GetMapping("/contact/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(contactService.getContact(id));
    }

    @DeleteMapping("/contact/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteContact(@PathVariable UUID id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(new MessageResponse("Deleted successfully"));
    }


}
