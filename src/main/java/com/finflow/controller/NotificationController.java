package com.finflow.controller;

import com.finflow.entity.Notification;
import com.finflow.entity.User;
import com.finflow.repository.NotificationRepository;
import com.finflow.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<List<Notification>> getAll() {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}