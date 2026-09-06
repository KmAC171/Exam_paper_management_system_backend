package com.exam_paper.backend.service.lecturer;

import com.exam_paper.backend.dto.lecturer.AddMarkingRequestDTO;
import com.exam_paper.backend.dto.lecturer.LecturerMarkingProcessDTO;
import com.exam_paper.backend.dto.lecturer.MarkingResponseDTO;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LecturerMarkingService {

    private final ExamPacketRepository examPacketRepository;
    private final MarkingRepository markingRepository;
    private final UserRepository userRepository;

    private Long parseId(String str) {
        if (str == null) return null;
        try {
            return Long.parseLong(str.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public String addMarkingScripts(AddMarkingRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Request cannot be null");
        }
        if (request.getPacketId() == null || request.getPacketId().isBlank()) {
            throw new RuntimeException("Packet ID is required");
        }
        if (request.getLecturerId() == null || request.getLecturerId().isBlank()) {
            throw new RuntimeException("Lecturer ID is required");
        }
        if (request.getTotalScripts() == null || request.getTotalScripts() <= 0) {
            throw new RuntimeException("Total scripts must be greater than 0");
        }

        Long pId = parseId(request.getPacketId());
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found: " + request.getPacketId()));

        Long lId = parseId(request.getLecturerId());
        User lecturer = (lId != null ? userRepository.findById(lId) : userRepository.findByUsername(request.getLecturerId()))
                .orElseThrow(() -> new RuntimeException("Lecturer not found: " + request.getLecturerId()));

        Optional<Marking> existingMarking = markingRepository.findByPacketPacketId(packet.getPacketId());
        Marking marking;

        if (existingMarking.isPresent()) {
            marking = existingMarking.get();
            marking.setLecturer(lecturer);
            marking.setTotalScripts(request.getTotalScripts());
        } else {
            marking = Marking.builder()
                    .markingId("MK" + UUID.randomUUID().toString().substring(0, 8))
                    .packet(packet)
                    .lecturer(lecturer)
                    .totalScripts(request.getTotalScripts())
                    .markedScripts(0)
                    .build();
        }

        markingRepository.save(marking);
        return "Total number of answer scripts added successfully";
    }

    public MarkingResponseDTO getMarkingByPacketId(String packetId) {
        Long pId = parseId(packetId);
        if (pId == null) {
            return new MarkingResponseDTO(packetId, 0);
        }

        Marking marking = markingRepository.findByPacketPacketId(pId).orElse(null);
        if (marking == null) {
            return new MarkingResponseDTO(packetId, 0);
        }

        return new MarkingResponseDTO(
                String.valueOf(marking.getPacket().getPacketId()),
                marking.getTotalScripts()
        );
    }

    public List<LecturerMarkingProcessDTO> getMarkingProcess(String lecturerId) {
        Long lId = parseId(lecturerId);
        if (lId == null) {
            User user = userRepository.findByUsername(lecturerId).orElse(null);
            if (user != null) lId = user.getUserId();
        }
        if (lId == null) return List.of();

        List<Marking> markings = markingRepository.findByLecturerUserId(lId);
        List<LecturerMarkingProcessDTO> response = new ArrayList<>();

        for (Marking marking : markings) {
            int totalScripts = marking.getTotalScripts() != null ? marking.getTotalScripts() : 0;
            int markedScripts = marking.getMarkedScripts() != null ? marking.getMarkedScripts() : 0;
            int remainingScripts = Math.max(0, totalScripts - markedScripts);
            double progress = totalScripts > 0 ? ((double) markedScripts / totalScripts) * 100 : 0.0;

            ExamPacket packet = marking.getPacket();
            String courseCode = (packet != null && packet.getCourse() != null) ? packet.getCourse().getCourseCode() : "";
            String courseName = (packet != null && packet.getCourse() != null) ? packet.getCourse().getCourseName() : "";
            String pId = (packet != null) ? String.valueOf(packet.getPacketId()) : "";

            response.add(new LecturerMarkingProcessDTO(
                    marking.getMarkingId(),
                    pId,
                    courseCode,
                    courseName,
                    totalScripts,
                    markedScripts,
                    remainingScripts,
                    progress,
                    marking.getDeadline()
            ));
        }

        return response;
    }
}
