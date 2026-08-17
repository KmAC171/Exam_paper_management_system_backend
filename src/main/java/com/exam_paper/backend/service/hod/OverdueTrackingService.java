package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.repository.ExamPacketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverdueTrackingService {

    private final ExamPacketRepository examPacketRepository;
    private final PacketWorkflowService packetWorkflowService;

    public List<DepartmentPacketResponseDto> getPreviousCycleRecords(String deptId) {
        return examPacketRepository.findAll().stream()
                .filter(p -> p.getCourse() != null && p.getCourse().getDepartment() != null &&
                        deptId.equalsIgnoreCase(p.getCourse().getDepartment().getDeptId()))
                .filter(p -> p.getAcademicCycle() != null &&
                        "Completed".equalsIgnoreCase(p.getAcademicCycle().getStatus()))
                .map(packetWorkflowService::mapToPacketResponseDto)
                .collect(Collectors.toList());
    }

    public List<DepartmentPacketResponseDto> getOverduePackets(String deptId) {
        return packetWorkflowService.getAllDepartmentPackets(deptId).stream()
                .filter(DepartmentPacketResponseDto::isOverdue)
                .collect(Collectors.toList());
    }
}