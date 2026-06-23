package com.exam_paper.backend.service;
import com.exam_paper.backend.dto.AddCommentDTO;

import com.exam_paper.backend.entity.Comment;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.CommentRepository;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository repo;
    private final ExamPacketRepository packetRepo;

    public void addComment(Long packetId, AddCommentDTO dto) {

        ExamPacket packet = packetRepo.findById(packetId)
                .orElseThrow();

        Comment c = new Comment();
        c.setPacket(packet);
        c.setCommentText(dto.getCommentText());
        c.setTimestamp(LocalDateTime.now());

        repo.save(c);
    }
}
