package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.CommentResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.PacketComment;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketCommentRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerCommentService {

    private final ExamPacketRepository examPacketRepository;
    private final UserRepository userRepository;
    private final PacketCommentRepository packetCommentRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public CommentResponseDTO addComment(String packetId, String userId, String commentText) {
        if (packetId == null || packetId.trim().isEmpty()) {
            throw new RuntimeException("Packet ID is required");
        }
        Long pId = parseId(packetId);
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : java.util.Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found: " + packetId));

        if (userId == null || userId.trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        Long uId = parseId(userId);
        User user = (uId != null ? userRepository.findById(uId) : userRepository.findByUsername(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (commentText == null || commentText.trim().isEmpty()) {
            throw new RuntimeException("Comment text is required");
        }

        PacketComment comment = PacketComment.builder()
                .packet(packet)
                .user(user)
                .comment(commentText.trim())
                .createdAt(LocalDateTime.now())
                .build();

        PacketComment saved = packetCommentRepository.save(comment);
        return convertToDTO(saved);
    }

    public List<CommentResponseDTO> getPacketComments(String packetId) {
        if (packetId == null || packetId.trim().isEmpty()) {
            throw new RuntimeException("Packet ID is required");
        }
        Long pId = parseId(packetId);
        if (pId == null) {
            return List.of();
        }
        List<PacketComment> comments = packetCommentRepository.findByPacket_PacketIdOrderByCreatedAtAsc(pId);
        return comments.stream().map(this::convertToDTO).toList();
    }

    private CommentResponseDTO convertToDTO(PacketComment comment) {
        return CommentResponseDTO.builder()
                .commentId(comment.getId() != null ? String.valueOf(comment.getId()) : null)
                .packetId(comment.getPacket() != null ? String.valueOf(comment.getPacket().getPacketId()) : null)
                .userId(comment.getUser() != null ? String.valueOf(comment.getUser().getUserId()) : null)
                .userName(comment.getUser() != null ? comment.getUser().getFullName() : "Unknown User")
                .commentText(comment.getComment())
                .timestamp(comment.getCreatedAt())
                .build();
    }
}
