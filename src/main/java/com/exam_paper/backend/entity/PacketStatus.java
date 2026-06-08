package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packet_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;

    private String statusName;
}