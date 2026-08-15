package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.service.lecturer.LecturerMarkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin
public class LecturerMarkingController {

    private final LecturerMarkingService lecturerMarkingService;

    @PostMapping("/marking")
    public ResponseEntity<String> addMarkingScripts(
            @RequestBody AddMarkingRequestDTO request
    ) {
        return ResponseEntity.ok(
                lecturerMarkingService.addMarkingScripts(request)
        );
    }

    @GetMapping("/marking/{packetId}")
    public ResponseEntity<MarkingResponseDTO> getMarkingByPacketId(
            @PathVariable String packetId
    ) {
        return ResponseEntity.ok(
                lecturerMarkingService.getMarkingByPacketId(packetId)
        );
    }

    @GetMapping("/{lecturerId}/marking-process")
    public ResponseEntity<List<LecturerMarkingProcessDTO>> getMarkingProcess(
            @PathVariable String lecturerId
    ) {
        return ResponseEntity.ok(
                lecturerMarkingService.getMarkingProcess(lecturerId)
        );
    }
}