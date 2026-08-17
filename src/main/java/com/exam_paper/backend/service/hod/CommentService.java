package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.CommentRequestDto;
import com.exam_paper.backend.dto.hod.CommentResponseDto;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final UserRepository userRepository;

    public CommentResponseDto addComment(CommentRequestDto dto) {
        ExamPacket packet = examPacketRepository.findById(dto.getPacketId())
                .orElseThrow(() -> new RuntimeException("Packet not found with ID: " + dto.getPacketId()));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        Comment comment = new Comment();
        comment.setCommentId("CMT" + UUID.randomUUID().toString().substring(0, 5));
        comment.setPacket(packet);
        comment.setUser(user);
        comment.setCommentText(dto.getCommentText());
        comment.setTimestamp(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        return CommentResponseDto.builder()
                .commentId(saved.getCommentId())
                .packetId(packet.getPacketId())
                .userName(user.getName())
                .commentText(saved.getCommentText())
                .timestamp(saved.getTimestamp())
                .build();
    }

    public List<CommentResponseDto> getPacketComments(String packetId) {
        return commentRepository.findAll().stream()
                .filter(c -> c.getPacket() != null && packetId.equalsIgnoreCase(c.getPacket().getPacketId()))
                .map(c -> CommentResponseDto.builder()
                        .commentId(c.getCommentId())
                        .packetId(packetId)
                        .userName(c.getUser() != null ? c.getUser().getName() : "Unknown")
                        .commentText(c.getCommentText())
                        .timestamp(c.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}