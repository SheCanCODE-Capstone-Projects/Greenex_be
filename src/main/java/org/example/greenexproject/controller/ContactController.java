package org.example.greenexproject.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.ContactRequest;
import org.example.greenexproject.dto.response.ContactResponse;
import org.example.greenexproject.Service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // Public endpoint for users to submit contact messages
    @PostMapping("/contact")
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.createContact(request);
        return ResponseEntity.ok(response);
    }

    // Admin endpoints (secure these in SecurityConfig)
    @GetMapping("/admin/contact")
    public ResponseEntity<List<ContactResponse>> listAll() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @GetMapping("/admin/contact/{id}")
    public ResponseEntity<ContactResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(contactService.getContact(id));
    }

    @PostMapping("/admin/contact/{id}/processed")
    public ResponseEntity<Void> markProcessed(@PathVariable UUID id) {
        contactService.markProcessed(id);
        return ResponseEntity.noContent().build();
    }
}
