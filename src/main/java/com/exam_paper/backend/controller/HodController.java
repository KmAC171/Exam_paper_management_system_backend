package com.exam_paper.backend.controller;


import com.exam_paper.backend.dto.*;
import com.exam_paper.backend.entity.Comment;
import com.exam_paper.backend.service.HodServiceImpl;


import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
@CrossOrigin("*")
public class HodController {



    private final HodServiceImpl hodService;



    // ===============================
    // View all packets
    // ===============================

    @GetMapping("/packets")
    public ResponseEntity<List<HodPacketResponseDTO>> getAllPackets(){


        return ResponseEntity.ok(
                hodService.getAllPackets()
        );

    }







    // ===============================
    // Packet details
    // ===============================

    @GetMapping("/packets/{packetId}")
    public ResponseEntity<PacketDetailsResponseDTO> getPacketDetails(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getPacketDetails(packetId)
        );

    }







    // ===============================
    // Packet history
    // ===============================

    @GetMapping("/packets/{packetId}/history")
    public ResponseEntity<List<PacketMovementResponseDTO>> getPacketHistory(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getPacketHistory(packetId)
        );

    }







    // ===============================
    // Search
    // ===============================

    @GetMapping("/packets/search")
    public ResponseEntity<List<HodPacketResponseDTO>> searchPackets(
            @RequestParam String keyword
    ){


        return ResponseEntity.ok(
                hodService.searchPackets(keyword)
        );

    }







    // ===============================
    // NEW ADVANCED FILTER
    // ===============================

    @GetMapping("/packets/filter")
    public ResponseEntity<List<HodPacketResponseDTO>> filterPackets(
            PacketFilterDTO filter
    ){


        return ResponseEntity.ok(
                hodService.filterPackets(filter)
        );

    }







    // ===============================
    // Staff workload
    // ===============================

    @GetMapping("/workload")
    public ResponseEntity<List<WorkloadResponseDTO>> getWorkload(){


        return ResponseEntity.ok(
                hodService.getStaffWorkload()
        );

    }







    // ===============================
    // Marking progress
    // ===============================

    @GetMapping("/marking-progress")
    public ResponseEntity<List<MarkingProgressResponseDTO>> getMarkingProgress(){


        return ResponseEntity.ok(
                hodService.getMarkingProgress()
        );

    }







    // ===============================
    // Comments
    // ===============================

    @PostMapping("/comments")
    public ResponseEntity<Comment> addComment(
            @RequestBody Comment comment
    ){


        return ResponseEntity.ok(
                hodService.addComment(comment)
        );

    }





    @GetMapping("/comments/{packetId}")
    public ResponseEntity<List<Comment>> getComments(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getComments(packetId)
        );

    }







    // ===============================
    // Last updated user
    // ===============================

    @GetMapping("/packets/{packetId}/last-updated-user")
    public ResponseEntity<LastUpdatedUserResponseDTO> getLastUpdatedUser(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getLastUpdatedUser(packetId)
        );

    }


}