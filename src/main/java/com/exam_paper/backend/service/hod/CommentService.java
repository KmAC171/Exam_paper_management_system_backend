package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.CommentRequestDto;
import com.exam_paper.backend.dto.hod.CommentResponseDto;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PacketCommentRepository commentRepository;
    private final ExamPacketRepository examPacketRepository;
    private final UserRepository userRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    public CommentResponseDto addComment(CommentRequestDto dto) {
        Long pId = parseId(dto.getPacketId());
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found with ID: " + dto.getPacketId()));

        Long uId = parseId(dto.getUserId());
        User user = (uId != null ? userRepository.findById(uId) : userRepository.findByUsername(dto.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        PacketComment comment = PacketComment.builder()
                .packet(packet)
                .user(user)
                .comment(dto.getCommentText())
                .createdAt(LocalDateTime.now())
                .build();

        PacketComment saved = commentRepository.save(comment);

        return CommentResponseDto.builder()
                .commentId(String.valueOf(saved.getId()))
                .packetId(String.valueOf(packet.getPacketId()))
                .userName(user.getFullName())
                .commentText(saved.getComment())
                .timestamp(saved.getCreatedAt())
                .build();
    }

    public List<CommentResponseDto> getPacketComments(String packetId) {
        Long pId = parseId(packetId);
        if (pId == null) return List.of();

        return commentRepository.findByPacket_PacketIdOrderByCreatedAtAsc(pId).stream()
                .map(c -> CommentResponseDto.builder()
                        .commentId(String.valueOf(c.getId()))
                        .packetId(packetId)
                        .userName(c.getUser() != null ? c.getUser().getFullName() : "Unknown")
                        .commentText(c.getComment())
                        .timestamp(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
