package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name="departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {


    @Id
    @Column(length = 10)
    private String deptId;


    @Column(nullable=false,length=100)
    private String deptName;


    @OneToMany(mappedBy = "department")
    private List<User> users;


    @OneToMany(mappedBy = "department")
    private List<Course> courses;

}