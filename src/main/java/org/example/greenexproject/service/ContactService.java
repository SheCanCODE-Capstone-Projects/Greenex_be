package org.example.greenexproject.service;

import org.example.greenexproject.dto.request.ContactRequest;
import org.example.greenexproject.dto.response.ContactResponse;

import java.util.List;
import java.util.UUID;

public interface ContactService {
    ContactResponse createContact(ContactRequest request);
    List<ContactResponse> getAllContacts();
    ContactResponse getContact(UUID id);
    void deleteContact(UUID id);
}
