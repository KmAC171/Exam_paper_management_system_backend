package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name="courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {


    @Id
    @Column(length=10)
    private String courseId;


    private String courseCode;


    private String courseName;



    @ManyToOne
    @JoinColumn(name="dept_id")
    private Department department;



    @OneToMany(mappedBy="course")
    private List<ExamPacket> packets;

}