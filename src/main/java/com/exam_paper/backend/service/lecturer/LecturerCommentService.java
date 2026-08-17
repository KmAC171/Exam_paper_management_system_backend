package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.CommentResponseDTO;
import com.exam_paper.backend.entity.Comment;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.CommentRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
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
    private final CommentRepository commentRepository;

    // =========================================================
    // ADD COMMENT
    // =========================================================

    public CommentResponseDTO addComment(
            String packetId,
            String userId,
            String commentText
    ) {

        // -----------------------------------------------------
        // Validate packet
        // -----------------------------------------------------

        if (packetId == null || packetId.trim().isEmpty()) {
            throw new RuntimeException("Packet ID is required");
        }

        ExamPacket packet = examPacketRepository
                .findById(packetId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Packet not found: " + packetId
                        )
                );

        // -----------------------------------------------------
        // Validate user
        // -----------------------------------------------------

        if (userId == null || userId.trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + userId
                        )
                );

        // -----------------------------------------------------
        // Validate comment
        // -----------------------------------------------------

        if (commentText == null || commentText.trim().isEmpty()) {
            throw new RuntimeException(
                    "Comment text is required"
            );
        }

        // -----------------------------------------------------
        // Create comment
        // -----------------------------------------------------

        Comment comment = Comment.builder()

                .commentId(generateCommentId())

                .packet(packet)

                .user(user)

                .commentText(commentText.trim())

                .timestamp(LocalDateTime.now())

                .build();

        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Comment saved = commentRepository.save(comment);

        // -----------------------------------------------------
        // Convert entity -> DTO
        // -----------------------------------------------------

        return convertToDTO(saved);
    }

    // =========================================================
    // GET COMMENTS FOR PACKET
    // =========================================================

    public List<CommentResponseDTO> getPacketComments(
            String packetId
    ) {

        if (packetId == null || packetId.trim().isEmpty()) {
            throw new RuntimeException("Packet ID is required");
        }

        // Make sure packet exists
        examPacketRepository
                .findById(packetId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Packet not found: " + packetId
                        )
                );

        List<Comment> comments =
                commentRepository
                        .findByPacketPacketIdOrderByTimestampAsc(
                                packetId
                        );

        return comments.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private CommentResponseDTO convertToDTO(
            Comment comment
    ) {

        return CommentResponseDTO.builder()

                .commentId(
                        comment.getCommentId()
                )

                .packetId(
                        comment.getPacket() != null
                                ? comment.getPacket().getPacketId()
                                : null
                )

                .userId(
                        comment.getUser() != null
                                ? comment.getUser().getUserId()
                                : null
                )

                .userName(
                        comment.getUser() != null
                                ? comment.getUser().getName()
                                : "Unknown User"
                )

                .commentText(
                        comment.getCommentText()
                )

                .timestamp(
                        comment.getTimestamp()
                )

                .build();
    }

    // =========================================================
    // GENERATE COMMENT ID
    // =========================================================

    private String generateCommentId() {

        long count = commentRepository.count();

        String commentId;

        do {
            count++;

            commentId = "CMT" + count;

        } while (commentRepository.existsById(commentId));

        return commentId;
    }
}