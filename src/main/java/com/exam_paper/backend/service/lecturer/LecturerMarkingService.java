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
    public MarkingResponseDTO addMarkingScripts(AddMarkingRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Request cannot be null");
        }
        if (request.getPacketId() == null || request.getPacketId().isBlank()) {
            throw new RuntimeException("Packet ID is required");
        }
        if (request.getLecturerId() == null || request.getLecturerId().isBlank()) {
            throw new RuntimeException("Lecturer ID is required");
        }

        Long pId = parseId(request.getPacketId());
        ExamPacket packet = (pId != null ? examPacketRepository.findById(pId) : Optional.<ExamPacket>empty())
                .orElseThrow(() -> new RuntimeException("Packet not found: " + request.getPacketId()));

        Long lId = parseId(request.getLecturerId());
        User lecturer = (lId != null ? userRepository.findById(lId) : userRepository.findByUsername(request.getLecturerId()))
                .orElseThrow(() -> new RuntimeException("Lecturer not found: " + request.getLecturerId()));

        if (packet.getLecturer() != null && !packet.getLecturer().getUserId().equals(lecturer.getUserId())
                && lecturer.getRole() != User.Role.ROLE_ADMIN && lecturer.getRole() != User.Role.ROLE_SYSTEM_ADMIN) {
            throw new IllegalArgumentException("Only the designated course lecturer can add or update marking scripts for this exam packet.");
        }

        Optional<Marking> existingMarking = markingRepository.findByPacketPacketId(packet.getPacketId());
        Marking marking;

        Integer effectiveTotal = request.getTotalScripts();
        if (effectiveTotal == null || effectiveTotal <= 0) {
            if (existingMarking.isPresent() && existingMarking.get().getTotalScripts() != null && existingMarking.get().getTotalScripts() > 0) {
                effectiveTotal = existingMarking.get().getTotalScripts();
            } else if (packet.getNumberOfCopies() != null && packet.getNumberOfCopies() > 0) {
                effectiveTotal = packet.getNumberOfCopies();
            } else {
                effectiveTotal = 50;
            }
        }

        Integer effectiveMarked = request.getMarkedScripts();
        if (effectiveMarked == null) {
            effectiveMarked = existingMarking.map(m -> m.getMarkedScripts() != null ? m.getMarkedScripts() : 0).orElse(0);
        }

        if (effectiveMarked < 0) {
            throw new IllegalArgumentException("Marked scripts cannot be negative.");
        }
        if (effectiveMarked > effectiveTotal) {
            throw new IllegalArgumentException("Marked scripts (" + effectiveMarked + ") cannot exceed total copies (" + effectiveTotal + ").");
        }

        if (existingMarking.isPresent()) {
            marking = existingMarking.get();
            marking.setLecturer(lecturer);
            marking.setTotalScripts(effectiveTotal);
            marking.setMarkedScripts(effectiveMarked);
        } else {
            marking = Marking.builder()
                    .markingId("MK" + UUID.randomUUID().toString().substring(0, 8))
                    .packet(packet)
                    .lecturer(lecturer)
                    .totalScripts(effectiveTotal)
                    .markedScripts(effectiveMarked)
                    .build();
        }

        packet.setNumberOfCopies(effectiveTotal);
        examPacketRepository.save(packet);
        markingRepository.save(marking);

        int remaining = Math.max(0, effectiveTotal - effectiveMarked);
        double progress = effectiveTotal > 0 ? ((double) effectiveMarked / effectiveTotal) * 100.0 : 0.0;

        return MarkingResponseDTO.builder()
                .packetId(String.valueOf(packet.getPacketId()))
                .totalScripts(effectiveTotal)
                .markedScripts(effectiveMarked)
                .remainingScripts(remaining)
                .progress(Math.round(progress * 10.0) / 10.0)
                .build();
    }

    public MarkingResponseDTO getMarkingByPacketId(String packetId) {
        Long pId = parseId(packetId);
        if (pId == null) {
            return new MarkingResponseDTO(packetId, 50, 0, 50, 0.0);
        }

        ExamPacket packet = examPacketRepository.findById(pId).orElse(null);
        int totalScripts = packet != null && packet.getNumberOfCopies() != null ? packet.getNumberOfCopies() : 50;
        int markedScripts = 0;

        Marking marking = markingRepository.findByPacketPacketId(pId).orElse(null);
        if (marking != null) {
            if (marking.getTotalScripts() != null && marking.getTotalScripts() > 0) {
                totalScripts = marking.getTotalScripts();
            }
            if (marking.getMarkedScripts() != null) {
                markedScripts = marking.getMarkedScripts();
            }
        }

        int remaining = Math.max(0, totalScripts - markedScripts);
        double progress = totalScripts > 0 ? ((double) markedScripts / totalScripts) * 100.0 : 0.0;

        return MarkingResponseDTO.builder()
                .packetId(packetId)
                .totalScripts(totalScripts)
                .markedScripts(markedScripts)
                .remainingScripts(remaining)
                .progress(Math.round(progress * 10.0) / 10.0)
                .build();
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
