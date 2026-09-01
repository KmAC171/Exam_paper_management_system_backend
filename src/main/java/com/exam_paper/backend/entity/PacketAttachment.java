package com.exam_paper.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "packet_attachments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PacketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;

    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    private LocalDateTime uploadedAt;
}