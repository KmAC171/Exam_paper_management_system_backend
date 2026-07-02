package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.PacketMovementDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketMovement;
import com.exam_paper.backend.entity.PacketStatus;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketMovementRepository;
import com.exam_paper.backend.repository.UserRepository;
import com.exam_paper.backend.repository.PacketStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacketMovementService {

    private final PacketMovementRepository movementRepository;
    private final ExamPacketRepository packetRepository;
    private final UserRepository userRepository;
    private final PacketStatusRepository statusRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    // =========================================================
    // SUBMIT PACKET (LECTURER -> MODERATOR)
    // =========================================================
    public void submitPacket(
            Long packetId,
            Long lecturerId,
            String remarks
    ) {

        ExamPacket packet = packetRepository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"
                        )
                );

        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Create movement record
        PacketMovement movement = new PacketMovement();
        movement.setPacket(packet);
        movement.setFromUser(lecturer);
        movement.setToUser(packet.getModerator());
        movement.setAction("Submitted");
        movement.setRemarks(remarks);

        movementRepository.save(movement);

        // Update packet status
        PacketStatus submittedStatus = statusRepository.findByStatusName("Submitted")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Status not found"
                        )
                );

        packet.setStatus(submittedStatus);
        packet.setCurrentHolder(packet.getModerator());
        packetRepository.save(packet);

        // Send notification to moderator
        notificationService.sendNotification(
                packet.getModerator().getUserId(),
                packetId,
                "Packet " + packet.getCourse().getCourseCode() + " has been submitted by " + lecturer.getFullName(),
                "Info",
                "STATUS_CHANGE"
        );

        // Log to audit
        auditLogService.logAction(
                lecturerId,
                "SUBMIT",
                "EXAM_PACKET",
                packetId,
                "Packet submitted by " + lecturer.getFullName()
        );
    }

    // =========================================================
    // APPROVE PACKET (MODERATOR)
    // =========================================================
    public void approvePacket(
            Long packetId,
            Long moderatorId,
            String remarks
    ) {

        ExamPacket packet = packetRepository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"
                        )
                );

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Create movement record
        PacketMovement movement = new PacketMovement();
        movement.setPacket(packet);
        movement.setFromUser(moderator);
        movement.setToUser(packet.getLecturer());
        movement.setAction("Approved");
        movement.setRemarks(remarks);

        movementRepository.save(movement);

        // Update packet status
        PacketStatus approvedStatus = statusRepository.findByStatusName("Approved")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Status not found"
                        )
                );

        packet.setStatus(approvedStatus);
        packet.setCurrentHolder(packet.getLecturer());
        packetRepository.save(packet);

        // Send notification to lecturer
        notificationService.sendNotification(
                packet.getLecturer().getUserId(),
                packetId,
                "Your packet " + packet.getCourse().getCourseCode() + " has been approved",
                "Success",
                "STATUS_CHANGE"
        );

        // Log to audit
        auditLogService.logAction(
                moderatorId,
                "APPROVE",
                "EXAM_PACKET",
                packetId,
                "Packet approved by " + moderator.getFullName()
        );
    }

    // =========================================================
    // RETURN PACKET (MODERATOR -> LECTURER)
    // =========================================================
    public void returnPacket(
            Long packetId,
            Long moderatorId,
            String remarks
    ) {

        ExamPacket packet = packetRepository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"
                        )
                );

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // Create movement record
        PacketMovement movement = new PacketMovement();
        movement.setPacket(packet);
        movement.setFromUser(moderator);
        movement.setToUser(packet.getLecturer());
        movement.setAction("Returned");
        movement.setRemarks(remarks);

        movementRepository.save(movement);

        // Update packet status
        PacketStatus returnedStatus = statusRepository.findByStatusName("Returned")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Status not found"
                        )
                );

        packet.setStatus(returnedStatus);
        packet.setCurrentHolder(packet.getLecturer());
        packetRepository.save(packet);

        // Send notification to lecturer
        notificationService.sendNotification(
                packet.getLecturer().getUserId(),
                packetId,
                "Your packet " + packet.getCourse().getCourseCode() + " has been returned. Reason: " + remarks,
                "Warning",
                "STATUS_CHANGE"
        );

        // Log to audit
        auditLogService.logAction(
                moderatorId,
                "RETURN",
                "EXAM_PACKET",
                packetId,
                "Packet returned by " + moderator.getFullName() + ". Reason: " + remarks
        );
    }

    // =========================================================
    // GET PACKET MOVEMENT HISTORY
    // =========================================================
    public List<PacketMovementDTO> getPacketMovementHistory(Long packetId) {

        return movementRepository.findByPacketPacketIdOrderByTimestampDesc(packetId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =========================================================
    // COMMON MAPPER
    // =========================================================
    private PacketMovementDTO mapToDTO(PacketMovement movement) {

        return new PacketMovementDTO(
                movement.getMovementId(),
                movement.getPacket().getPacketId(),
                movement.getFromUser().getFullName(),
                movement.getToUser() != null ? movement.getToUser().getFullName() : "System",
                movement.getAction(),
                movement.getTimestamp(),
                movement.getRemarks()
        );
    }
}
