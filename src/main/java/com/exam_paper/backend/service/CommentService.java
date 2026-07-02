package com.exam_paper.backend.service;

import com.exam_paper.backend.dto.CommentDTO;
import com.exam_paper.backend.dto.CreateCommentDTO;
import com.exam_paper.backend.entity.Comment;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.CommentRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ExamPacketRepository packetRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    // =========================================================
    // ADD COMMENT ON PACKET
    // =========================================================
    public CommentDTO addComment(
            Long packetId,
            Long userId,
            CreateCommentDTO dto
    ) {

        ExamPacket packet = packetRepository.findById(packetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Packet not found"
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        Comment comment = new Comment();
        comment.setPacket(packet);
        comment.setUser(user);
        comment.setCommentText(dto.getCommentText());

        Comment saved = commentRepository.save(comment);

        // Log to audit
        auditLogService.logAction(
                userId,
                "COMMENT",
                "EXAM_PACKET",
                packetId,
                "User " + user.getFullName() + " commented on packet"
        );

        return mapToDTO(saved);
    }

    // =========================================================
    // GET ALL COMMENTS FOR A PACKET
    // =========================================================
    public List<CommentDTO> getPacketComments(Long packetId) {

        return commentRepository.findByPacketPacketIdOrderByTimestampDesc(packetId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================
    public void deleteComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Comment not found"
                        )
                );

        // Only the comment author or admin can delete
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete this comment"
            );
        }

        commentRepository.deleteById(commentId);

        auditLogService.logAction(
                userId,
                "DELETE",
                "COMMENT",
                commentId,
                "Comment deleted"
        );
    }

    // =========================================================
    // COMMON MAPPER
    // =========================================================
    private CommentDTO mapToDTO(Comment comment) {

        return new CommentDTO(
                comment.getCommentId(),
                comment.getPacket().getPacketId(),
                comment.getUser().getUserId(),
                comment.getUser().getFullName(),
                comment.getCommentText(),
                comment.getTimestamp()
        );
    }
}
