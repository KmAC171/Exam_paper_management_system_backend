package com.exam_paper.backend.controller.lecturer;

import com.exam_paper.backend.dto.lecturer.AddMarkingRequestDTO;
import com.exam_paper.backend.dto.lecturer.LecturerMarkingProcessDTO;
import com.exam_paper.backend.dto.lecturer.MarkingResponseDTO;
import com.exam_paper.backend.service.lecturer.LecturerMarkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LecturerMarkingController {

    private final LecturerMarkingService lecturerMarkingService;

    @PostMapping("/marking")
    public ResponseEntity<String> addMarkingScripts(@RequestBody AddMarkingRequestDTO request) {
        String response = lecturerMarkingService.addMarkingScripts(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/marking/{packetId}")
    public ResponseEntity<MarkingResponseDTO> getMarkingByPacketId(@PathVariable String packetId) {
        MarkingResponseDTO response = lecturerMarkingService.getMarkingByPacketId(packetId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{lecturerId}/marking-process")
    public ResponseEntity<List<LecturerMarkingProcessDTO>> getMarkingProcess(@PathVariable String lecturerId) {
        List<LecturerMarkingProcessDTO> response = lecturerMarkingService.getMarkingProcess(lecturerId);
        return ResponseEntity.ok(response);
    }
}
