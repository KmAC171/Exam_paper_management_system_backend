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
public class DelayReportGenerator {



    private final ExamPacketRepository examPacketRepository;




    public ReportResponseDTO generateDelayReport(){



        List<ExamPacket> packets =
                examPacketRepository.findAll();



        long totalPackets =
                packets.size();



        long delayedPackets =
                packets.stream()

                        .filter(packet ->
                                packet.getStatus()
                                        .equalsIgnoreCase("Delayed"))

                        .count();




        long overduePackets =
                packets.stream()

                        .filter(packet ->

                                packet.getDeadline() != null
                                        &&
                                        packet.getDeadline()
                                                .isBefore(LocalDate.now())

                                        &&
                                        !packet.getStatus()
                                                .equalsIgnoreCase("Completed")

                        )

                        .count();





        return new ReportResponseDTO(

                "Delay Report",

                totalPackets,

                0L,

                0L,

                overduePackets,

                LocalDate.now()
                        .toString()

        );


    }


}