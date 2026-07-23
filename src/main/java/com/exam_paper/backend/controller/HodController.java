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
    // View packet details
    // ===============================

    @GetMapping("/packets/id/{packetId}")
    public ResponseEntity<PacketDetailsResponseDTO> getPacketDetails(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getPacketDetails(packetId)
        );

    }







    // ===============================
    // View packet movement history
    // ===============================

    @GetMapping("/packets/id/{packetId}/history")
    public ResponseEntity<List<PacketMovementResponseDTO>> getPacketHistory(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getPacketHistory(packetId)
        );

    }







    // ===============================
    // Search packets
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
    // Advanced packet filter
    // status
    // academic cycle
    // lecturer
    // moderator
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
    // Add comment
    // ===============================

    @PostMapping("/comments")
    public ResponseEntity<Comment> addComment(
            @RequestBody Comment comment
    ){


        return ResponseEntity.ok(
                hodService.addComment(comment)
        );

    }







    // ===============================
    // View communication history
    // ===============================

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

    @GetMapping("/packets/id/{packetId}/last-updated-user")
    public ResponseEntity<LastUpdatedUserResponseDTO> getLastUpdatedUser(
            @PathVariable Long packetId
    ){


        return ResponseEntity.ok(
                hodService.getLastUpdatedUser(packetId)
        );

    }







    // =====================================
    // Previous academic cycles
    // =====================================

    @GetMapping("/academic-cycles/previous")
    public ResponseEntity<List<AcademicCycleResponseDTO>>
    getPreviousCycles(){


        return ResponseEntity.ok(
                hodService.getPreviousCycles()
        );

    }







    // =====================================
    // Previous academic cycle packets
    // =====================================

    @GetMapping("/packets/previous/{cycleId}")
    public ResponseEntity<List<HodPacketResponseDTO>>
    getPreviousCyclePackets(
            @PathVariable Long cycleId
    ){


        return ResponseEntity.ok(
                hodService.getPreviousCyclePackets(cycleId)
        );

    }







    // =====================================
    // Overdue packets
    // =====================================

    @GetMapping("/packets/overdue")
    public ResponseEntity<List<HodPacketResponseDTO>>
    getOverduePackets(){


        return ResponseEntity.ok(
                hodService.getOverduePackets()
        );

    }







    // =====================================
    // Delayed packets
    // =====================================

    @GetMapping("/packets/delayed")
    public ResponseEntity<List<HodPacketResponseDTO>>
    getDelayedPackets(){


        return ResponseEntity.ok(
                hodService.getDelayedPackets()
        );

    }


}