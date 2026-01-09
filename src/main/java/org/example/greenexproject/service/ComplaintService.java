package org.example.greenexproject.service;

import org.example.greenexproject.dto.request.ComplaintRequest;
import org.example.greenexproject.dto.response.ComplaintResponse;

public interface ComplaintService {
    ComplaintResponse createComplaint(ComplaintRequest request);
}
