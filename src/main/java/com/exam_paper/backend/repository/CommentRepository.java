package com.exam_paper.backend.repository;

import com.exam_paper.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPacketPacketIdOrderByTimestampDesc(Long packetId);
}