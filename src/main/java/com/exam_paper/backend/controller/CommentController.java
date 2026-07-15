package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.CommentDTO;
import com.exam_paper.backend.dto.CreateCommentDTO;
import com.exam_paper.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    // =========================================================
    // ADD COMMENT
    // POST /api/comments?packetId=1&userId=1
    // =========================================================
    @PostMapping
    public CommentDTO addComment(
            @RequestParam Long packetId,
            @RequestParam Long userId,
            @RequestBody CreateCommentDTO dto
    ) {
        return service.addComment(packetId, userId, dto);
    }

    // =========================================================
    // GET ALL COMMENTS FOR PACKET
    // GET /api/comments/packet/{packetId}
    // =========================================================
    @GetMapping("/packet/{packetId}")
    public List<CommentDTO> getPacketComments(
            @PathVariable Long packetId
    ) {
        return service.getPacketComments(packetId);
    }

    // =========================================================
    // DELETE COMMENT
    // DELETE /api/comments/{commentId}?userId=1
    // =========================================================
    @DeleteMapping("/{commentId}")
    public String deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        service.deleteComment(commentId, userId);
        return "Comment deleted successfully";
    }
}