package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name="roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {


    @Id
    @Column(length = 10)
    private String roleId;


    @Column(nullable = false,length = 50)
    private String roleName;


    @OneToMany(mappedBy = "role")
    private List<User> users;

}