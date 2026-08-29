package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.CreatePacketDTO;
import com.exam_paper.backend.dto.PacketDTO;
import com.exam_paper.backend.dto.PacketDetailDTO;
import com.exam_paper.backend.service.PacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

@RestController
@RequestMapping("/api/packets")
@RequiredArgsConstructor
public class PacketController {

    private final PacketService packetService;

    @GetMapping
    public List<PacketDTO> getPackets(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return packetService.getPackets(username, role);
    }

    @GetMapping("/{id}")
    public PacketDetailDTO getPacketDetail(@PathVariable Long id) {
        return packetService.getPacketDetail(id);
    }

    @PostMapping
    public PacketDTO createPacket(@RequestBody CreatePacketDTO dto) {
        return packetService.createPacket(dto);
    }

    @PutMapping("/{id}")
    public PacketDTO updatePacket(@PathVariable Long id, @RequestBody CreatePacketDTO dto) {
        return packetService.updatePacket(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletePacket(@PathVariable Long id) {
        packetService.deletePacket(id);
    }

    @GetMapping("/export/csv")
    public void exportCsv(Authentication authentication,
                          HttpServletResponse response) throws IOException {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        List<PacketDTO> packets = packetService.getPackets(username, role);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=packets.csv");

        PrintWriter writer = response.getWriter();
        writer.println("Packet ID,Course Code,Course Name,Lecturer,Moderator,Deadline,Status,Priority");

        for (PacketDTO p : packets) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    p.getPacketId(), p.getCourseCode(), p.getCourseName(),
                    p.getLecturerName(), p.getModeratorName(),
                    p.getDeadline(), p.getStatus(), p.getPriority());
        }
        writer.flush();
    }
}