package org.example.greenexproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.response.NotificationResponse;
import org.example.greenexproject.dto.request.NotificationRequest;
import org.example.greenexproject.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping
    public Page<NotificationResponse> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationService.getAllNotifications(pageable);
    }


    @PostMapping
    public NotificationResponse createNotification(@RequestBody NotificationRequest request) {
        return notificationService.createNotification(request);
    }


    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
    }
}
