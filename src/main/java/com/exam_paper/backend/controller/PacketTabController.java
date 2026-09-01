package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.PacketAttachment;
import com.exam_paper.backend.repository.PacketAttachmentRepository;
import com.exam_paper.backend.service.PacketTabService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packets")
@RequiredArgsConstructor
public class PacketTabController {

    private final PacketTabService tabService;
    private final PacketAttachmentRepository attachmentRepository;

    // ─── COMMENTS ─────────────────────────────────────────

    @GetMapping("/{id}/comments")
    public List<CommentDTO> getComments(@PathVariable Long id) {
        return tabService.getComments(id);
    }

    @PostMapping("/{id}/comments")
    public CommentDTO addComment(@PathVariable Long id,
                                 @RequestBody Map<String, String> body,
                                 Authentication auth) {
        return tabService.addComment(id, body.get("comment"), auth.getName());
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        tabService.deleteComment(commentId);
    }

    // ─── ATTACHMENTS ──────────────────────────────────────

    @GetMapping("/{id}/attachments")
    public List<AttachmentDTO> getAttachments(@PathVariable Long id) {
        return tabService.getAttachments(id);
    }

    @PostMapping("/{id}/attachments")
    public AttachmentDTO uploadAttachment(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file,
                                          Authentication auth) throws IOException {
        return tabService.uploadAttachment(id, file, auth.getName());
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public void deleteAttachment(@PathVariable Long attachmentId) {
        tabService.deleteAttachment(attachmentId);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        PacketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        File file = new File(attachment.getFilePath());
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(
                        attachment.getFileType() != null
                                ? attachment.getFileType()
                                : "application/octet-stream"))
                .body(resource);
    }

    // ─── HISTORY ──────────────────────────────────────────

    @GetMapping("/{id}/history")
    public List<HistoryDTO> getHistory(@PathVariable Long id) {
        return tabService.getHistory(id);
    }
}