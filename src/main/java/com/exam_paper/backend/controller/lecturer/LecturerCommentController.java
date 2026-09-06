package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.AddCommentRequestDTO;
import com.exam_paper.backend.dto.lecturer.CommentResponseDTO;
import com.exam_paper.backend.service.lecturer.LecturerCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LecturerCommentController {

    private final LecturerCommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDTO> addComment(@RequestBody AddCommentRequestDTO request) {
        CommentResponseDTO response = commentService.addComment(
                request.getPacketId(),
                request.getUserId(),
                request.getCommentText()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{packetId}")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable String packetId) {
        List<CommentResponseDTO> comments = commentService.getPacketComments(packetId);
        return ResponseEntity.ok(comments);
    }
}
