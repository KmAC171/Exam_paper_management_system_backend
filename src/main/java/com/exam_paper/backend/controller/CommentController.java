package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.*;

import com.exam_paper.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping("/{packetId}")
    public String add(@PathVariable Long packetId,
                      @RequestBody AddCommentDTO dto) {
        service.addComment(packetId, dto);
        return "Comment added";
    }
}