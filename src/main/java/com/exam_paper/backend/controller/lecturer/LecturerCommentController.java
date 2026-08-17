package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.CommentRequestDTO;
import com.exam_paper.backend.dto.lecturer.CommentResponseDTO;
import com.exam_paper.backend.service.lecturer.LecturerCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerCommentController {

    private final LecturerCommentService lecturerCommentService;

    // =========================================================
    // ADD COMMENT
    // POST /api/lecturer/comments
    // =========================================================

    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @RequestBody CommentRequestDTO request
    ) {

        CommentResponseDTO response =
                lecturerCommentService.addComment(
                        request.getPacketId(),
                        request.getUserId(),
                        request.getCommentText()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // GET COMMENTS
    // GET /api/lecturer/comments/{packetId}
    // =========================================================

    @GetMapping("/comments/{packetId}")
    public ResponseEntity<List<CommentResponseDTO>> getPacketComments(
            @PathVariable String packetId
    ) {

        return ResponseEntity.ok(
                lecturerCommentService
                        .getPacketComments(packetId)
        );
    }
}