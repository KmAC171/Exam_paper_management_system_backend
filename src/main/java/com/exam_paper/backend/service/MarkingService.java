package com.exam_paper.backend.service;


import com.exam_paper.backend.dto.MarkingDTO;
import com.exam_paper.backend.dto.MarkingResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.User;

import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class MarkingService {



    private final MarkingRepository markingRepository;

    private final ExamPacketRepository packetRepository;

    private final UserRepository userRepository;



    public MarkingResponseDTO addScriptCount(
            Long packetId,
            Long lecturerId,
            MarkingDTO dto
    ){


        ExamPacket packet =
                packetRepository.findById(packetId)
                        .orElseThrow(
                                () -> new RuntimeException("Packet not found")
                        );



        User lecturer =
                userRepository.findById(lecturerId)
                        .orElseThrow(
                                () -> new RuntimeException("Lecturer not found")
                        );



        Marking marking =
                markingRepository
                        .findByPacketPacketId(packetId);



        if(marking == null){


            marking = new Marking();

            marking.setPacket(packet);

            marking.setLecturer(lecturer);

            marking.setCompletedScripts(0);

        }



        marking.setScriptCount(
                dto.getScriptCount()
        );



        Marking saved =
                markingRepository.save(marking);



        return new MarkingResponseDTO(

                saved.getMarkingId(),

                saved.getPacket().getPacketId(),

                saved.getScriptCount(),

                saved.getCompletedScripts(),

                saved.getCreatedAt()

        );


    }



    public MarkingResponseDTO getMarking(
            Long packetId
    ){


        Marking marking =
                markingRepository
                        .findByPacketPacketId(packetId);



        if(marking == null){

            return null;

        }



        return new MarkingResponseDTO(

                marking.getMarkingId(),

                marking.getPacket().getPacketId(),

                marking.getScriptCount(),

                marking.getCompletedScripts(),

                marking.getCreatedAt()

        );


    }


}