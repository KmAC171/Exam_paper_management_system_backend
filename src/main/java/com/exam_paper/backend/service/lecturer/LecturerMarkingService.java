package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.*;
import com.exam_paper.backend.entity.ExamPacket;
import com.exam_paper.backend.entity.Marking;
import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LecturerMarkingService {

    private final ExamPacketRepository examPacketRepository;
    private final MarkingRepository markingRepository;
    private final UserRepository userRepository;


    /**
     * Add or update total script details for a marking entry.
     */
    @Transactional
    public String addMarkingScripts(AddMarkingRequestDTO request) {

        // Validate request
        if (request == null) {
            throw new RuntimeException("Request cannot be null");
        }

        if (request.getPacketId() == null ||
                request.getPacketId().isBlank()) {

            throw new RuntimeException("Packet ID is required");
        }

        if (request.getLecturerId() == null ||
                request.getLecturerId().isBlank()) {

            throw new RuntimeException("Lecturer ID is required");
        }

        if (request.getTotalScripts() == null ||
                request.getTotalScripts() <= 0) {

            throw new RuntimeException(
                    "Total scripts must be greater than 0"
            );
        }


        // Find packet using the correct ID
        ExamPacket packet =
                examPacketRepository
                        .findById(request.getPacketId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Packet not found: "
                                                + request.getPacketId()
                                                + ". Please ensure the database contains this ID (e.g., 'P1')."
                                )
                        );


        // Find lecturer
        User lecturer =
                userRepository
                        .findById(request.getLecturerId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Lecturer not found: "
                                                + request.getLecturerId()
                                )
                        );


        // Check whether marking already exists
        Optional<Marking> existingMarking =
                markingRepository
                        .findByPacketPacketId(
                                request.getPacketId()
                        );

        Marking marking;


        if (existingMarking.isPresent()) {

            // Update existing marking
            marking = existingMarking.get();

            marking.setLecturer(lecturer);

            marking.setTotalScripts(
                    request.getTotalScripts()
            );

        } else {

            // Create new marking
            marking = Marking.builder()

                    .markingId(
                            "MK" + System.currentTimeMillis()
                    )

                    .packet(packet)

                    .lecturer(lecturer)

                    .totalScripts(
                            request.getTotalScripts()
                    )

                    .build();
        }


        markingRepository.save(marking);

        return "Total number of answer scripts added successfully";
    }


    public MarkingResponseDTO getMarkingByPacketId(
            String packetId
    ) {

        Marking marking =
                markingRepository
                        .findByPacketPacketId(packetId)
                        .orElse(null);


        if (marking == null) {

            return new MarkingResponseDTO(
                    packetId,
                    0
            );
        }


        return new MarkingResponseDTO(
                marking.getPacket().getPacketId(),
                marking.getTotalScripts()
        );
    }


    /**
     * Get marking process status for the lecturer
     * without markedScripts calculations.
     */
    public List<LecturerMarkingProcessDTO> getMarkingProcess(
            String lecturerId
    ) {

        List<Marking> markings =
                markingRepository
                        .findByLecturerUserId(lecturerId);

        List<LecturerMarkingProcessDTO> response =
                new ArrayList<>();


        for (Marking marking : markings) {

            int totalScripts =
                    marking.getTotalScripts();

            int markedScripts = 0;

            int remainingScripts =
                    totalScripts - markedScripts;

            double progress = 0.0;


            response.add(

                    new LecturerMarkingProcessDTO(

                            marking.getMarkingId(),

                            marking.getPacket()
                                    .getPacketId(),

                            marking.getPacket()
                                    .getCourse()
                                    .getCourseCode(),

                            marking.getPacket()
                                    .getCourse()
                                    .getCourseName(),

                            totalScripts,

                            markedScripts,

                            remainingScripts,

                            progress,

                            marking.getDeadline()

                    )
            );
        }


        return response;
    }
}