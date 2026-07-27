package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.PacketDTO;
import com.exam_paper.backend.service.PacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packets")
@RequiredArgsConstructor
public class PacketController {

    private final PacketService packetService;

    @GetMapping
    public List<PacketDTO> getPackets(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator().next().getAuthority();
        return packetService.getPackets(username, role);
    }
}