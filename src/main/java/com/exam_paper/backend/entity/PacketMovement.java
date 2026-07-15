package com.exam_paper.backend.entity;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;



@Entity
@Table(name = "packet_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketMovement {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movementId;



    @ManyToOne
    @JoinColumn(name = "packet_id")
    private ExamPacket packet;



    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;



    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;



    private String action;



    private LocalDateTime timestamp;


}