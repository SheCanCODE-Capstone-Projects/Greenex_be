package org.example.greenexproject.service;

import org.example.greenexproject.dto.request.ComplaintRequest;
import org.example.greenexproject.model.entity.Complaint;

public interface ComplaintService {
    Complaint createComplaint(ComplaintRequest request);
}
