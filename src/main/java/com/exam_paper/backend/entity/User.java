package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 10)
    private String userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Added for BCrypt password hashing

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    @OneToMany(mappedBy = "currentHolder")
    private List<ExamPacket> packets;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;
}