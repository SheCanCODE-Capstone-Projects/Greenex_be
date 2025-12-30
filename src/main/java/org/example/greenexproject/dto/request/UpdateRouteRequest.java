package org.example.greenexproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.DayOfWeek;
import org.example.greenexproject.model.enums.RouteStatus;
import org.example.greenexproject.model.enums.Shift;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRouteRequest {
    private String name;

    private DayOfWeek dayOfWeek;

    private Shift shift;

    private RouteStatus status;
}
