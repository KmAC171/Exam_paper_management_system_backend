package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.AssignedPacketDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class AssignedExamPacketService {


    private final ExamPacketRepository repository;



    public List<AssignedPacketDTO> getAssignedPackets(Long lecturerId){


        return repository
                .findPacketsAssignedToLecturer(lecturerId)
                .stream()
                .map(this::convertToDTO)
                .toList();


    }





    private AssignedPacketDTO convertToDTO(
            ExamPacket packet
    ){


        return new AssignedPacketDTO(

                packet.getPacketId(),

                packet.getCourse()
                        .getCourseCode(),

                packet.getCourse()
                        .getCourseName(),

                packet.getStatus(),

                packet.getDeadline(),

                packet.getCurrentHolder()
                        .getFullName()

        );

    }


}