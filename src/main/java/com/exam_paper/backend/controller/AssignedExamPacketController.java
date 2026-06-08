package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.service.AssignedExamPacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packets")
@RequiredArgsConstructor
public class AssignedExamPacketController {

    private final AssignedExamPacketService service;

    // GET assigned packets for lecturer
    @GetMapping("/lecturer/{lecturerId}")
    public List<AssignedPacketDTO> getAssignedPackets(@PathVariable Long lecturerId) {
        return service.getAssignedPackets(lecturerId);
    }
}