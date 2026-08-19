package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public enum Role {
        ROLE_ADMIN,
        ROLE_MODERATOR,
        ROLE_USER,
        ROLE_GUEST
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String email;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private boolean isActive = true;
}