package com.exam_paper.backend.report;


import com.exam_paper.backend.dto.ReportResponseDTO;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.repository.ExamPacketRepository;


import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.util.List;



@Component
@RequiredArgsConstructor
public class ProgressReportGenerator {



    private final ExamPacketRepository examPacketRepository;



    public ReportResponseDTO generateProgressReport(){



        List<ExamPacket> packets =
                examPacketRepository.findAll();



        long totalPackets =
                packets.size();



        long completedPackets =
                packets.stream()

                        .filter(packet ->
                                packet.getStatus()
                                        .equalsIgnoreCase("Completed"))

                        .count();



        long pendingPackets =
                packets.stream()

                        .filter(packet ->
                                packet.getStatus()
                                        .equalsIgnoreCase("Pending"))

                        .count();



        long overduePackets =
                packets.stream()

                        .filter(packet ->
                                packet.getStatus()
                                        .equalsIgnoreCase("Overdue"))

                        .count();



        return new ReportResponseDTO(

                "Progress Report",

                totalPackets,

                completedPackets,

                pendingPackets,

                overduePackets,

                LocalDate.now()
                        .toString()

        );


    }

}