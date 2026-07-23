package com.exam_paper.backend.controller;


import com.exam_paper.backend.dto.MarkingDTO;
import com.exam_paper.backend.dto.MarkingResponseDTO;
import com.exam_paper.backend.service.MarkingService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/markings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MarkingController {



    private final MarkingService service;



    @PostMapping("/{packetId}")
    public MarkingResponseDTO addScripts(

            @PathVariable Long packetId,

            @RequestParam Long lecturerId,

            @RequestBody MarkingDTO dto

    ){


        return service.addScriptCount(
                packetId,
                lecturerId,
                dto
        );

    }



    @GetMapping("/{packetId}")
    public MarkingResponseDTO getMarking(

            @PathVariable Long packetId

    ){


        return service.getMarking(packetId);

    }


}