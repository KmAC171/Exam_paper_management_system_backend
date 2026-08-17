package com.exam_paper.backend.controller.hod;

import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.exam_paper.backend.service.hod.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkloadController {

    private final WorkloadService workloadService;

    @GetMapping("/department/{deptId}/workload")
    public ResponseEntity<List<LecturerWorkloadDto>> getDepartmentWorkload(@PathVariable String deptId) {
        return ResponseEntity.ok(workloadService.getDepartmentWorkload(deptId));
    }
}