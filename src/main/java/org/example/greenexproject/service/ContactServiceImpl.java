package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.ContactRequest;
import org.example.greenexproject.dto.response.ContactResponse;
import org.example.greenexproject.model.entity.Contact;
import org.example.greenexproject.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactServiceImpl implements ContactService {

    private final ContactRepository repository;

    @Override
    public ContactResponse createContact(ContactRequest request) {
        Contact contact = Contact.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .serviceInterest(request.getServiceInterest())
                .message(request.getMessage())
                .build();

        Contact saved = repository.save(contact);
        return mapToResponse(saved);
    }

    @Override
    public List<ContactResponse> getAllContacts() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ContactResponse getContact(UUID id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Contact message not found"));
    }


    @Override
    public void deleteContact(UUID id) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found"));
        repository.delete(contact);


    }

    private ContactResponse mapToResponse(Contact m) {
        return ContactResponse.builder()
                .id(m.getId())
                .fullName(m.getFullName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .serviceInterest(m.getServiceInterest())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .processed(m.isProcessed())
                .build();
    }
}
