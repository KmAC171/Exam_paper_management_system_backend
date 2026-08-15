package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.service.lecturer.LecturerCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerCommentController {

    private final LecturerCommentService lecturerCommentService;

    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @RequestBody CommentRequestDTO request
    ) {
        return ResponseEntity.ok(
                lecturerCommentService.addComment(
                        request.getPacketId(),
                        request.getUserId(),
                        request.getCommentText()
                )
        );
    }

    @GetMapping("/comments/{packetId}")
    public ResponseEntity<List<CommentResponseDTO>> getPacketComments(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerCommentService.getPacketComments(packetId)
        );
    }
}