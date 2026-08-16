package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
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


    public CommentResponseDTO addComment(
            String packetId,
            String userId,
            String text
    ) {

        ExamPacket packet =
                examPacketRepository
                        .findById(packetId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Packet not found"
                                )
                        );


        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );


        Comment comment =
                Comment.builder()

                        .commentId(
                                "CMT" +
                                        System.currentTimeMillis()
                        )

                        .packet(packet)

                        .user(user)

                        .commentText(text)

                        .timestamp(
                                LocalDateTime.now()
                        )

                        .build();


        Comment saved =
                commentRepository.save(comment);


        return CommentResponseDTO.builder()

                .commentId(
                        saved.getCommentId()
                )

                .packetId(
                        saved.getPacket()
                                .getPacketId()
                )

                .userId(
                        saved.getUser()
                                .getUserId()
                )

                .userName(
                        saved.getUser()
                                .getName()
                )

                .commentText(
                        saved.getCommentText()
                )

                .timestamp(
                        saved.getTimestamp()
                )

                .build();
    }


    /*
        Add comment using request DTO
    */
    public Comment addComment(
            AddCommentRequestDTO request
    ) {

        ExamPacket packet =
                examPacketRepository
                        .findById(
                                request.getPacketId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Packet not found"
                                )
                        );


        User user =
                userRepository
                        .findById(
                                request.getUserId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );


        Comment comment =
                Comment.builder()

                        .commentId(
                                generateCommentId()
                        )

                        .packet(packet)

                        .user(user)

                        .commentText(
                                request.getCommentText()
                        )

                        .timestamp(
                                LocalDateTime.now()
                        )

                        .build();


        return commentRepository.save(comment);
    }


    private String generateCommentId() {

        long count =
                commentRepository.count();

        return "CMT" + (count + 1);
    }


    /*
        View all comments of a packet
    */
    public List<CommentResponseDTO> getPacketComments(
            String packetId
    ) {

        List<Comment> comments =
                commentRepository
                        .findByPacketPacketId(packetId);


        return comments.stream()

                .map(comment ->
                        CommentResponseDTO.builder()

                                .commentId(
                                        comment.getCommentId()
                                )

                                .commentText(
                                        comment.getCommentText()
                                )

                                .timestamp(
                                        comment.getTimestamp()
                                )

                                .packetId(
                                        comment.getPacket()
                                                .getPacketId()
                                )

                                .userId(
                                        comment.getUser()
                                                .getUserId()
                                )

                                .userName(
                                        comment.getUser()
                                                .getName()
                                )

                                .build()
                )

                .toList();
    }
}