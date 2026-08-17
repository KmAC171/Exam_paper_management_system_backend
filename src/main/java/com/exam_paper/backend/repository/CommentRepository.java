package com.exam_paper.backend.repository;


import com.exam_paper.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentRepository
        extends JpaRepository<Comment,String>{





    // Get all comments related to one packet
    List<Comment> findByPacketPacketId(String packetId);
    List<Comment> findByPacketPacketIdOrderByTimestampAsc(String packetId);


}