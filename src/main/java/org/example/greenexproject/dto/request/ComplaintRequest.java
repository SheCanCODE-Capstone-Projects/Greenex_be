package org.example.greenexproject.dto.request;

import lombok.Data;
import java.util.UUID;
import org.example.greenexproject.model.enums.ComplaintType;

@Data
public class ComplaintRequest {
    private UUID householdId;
    private ComplaintType type;
    private String description;
}
