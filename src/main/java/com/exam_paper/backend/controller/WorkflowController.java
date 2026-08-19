package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.WorkflowPacketDTO;
import com.exam_paper.backend.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public List<WorkflowPacketDTO> getWorkflow(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator().next().getAuthority();
        return workflowService.getWorkflowPackets(username, role);
    }
}