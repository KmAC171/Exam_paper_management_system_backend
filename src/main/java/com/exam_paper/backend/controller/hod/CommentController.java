package com.exam_paper.backend.controller.hod;

import com.exam_paper.backend.dto.hod.CommentRequestDto;
import com.exam_paper.backend.dto.hod.CommentResponseDto;
import com.exam_paper.backend.service.hod.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comment")
    public ResponseEntity<CommentResponseDto> addComment(@RequestBody CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok(commentService.addComment(commentRequestDto));
    }

    @GetMapping("/packet/{packetId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getPacketComments(@PathVariable String packetId) {
        return ResponseEntity.ok(commentService.getPacketComments(packetId));
    }
}
