package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.DepartmentPacketResponseDto;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.ExamPacketRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.PacketMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacketWorkflowServiceTest {

    @Mock
    private ExamPacketRepository examPacketRepository;

    @Mock
    private PacketMovementRepository packetMovementRepository;

    @Mock
    private PacketAssignmentRepository packetAssignmentRepository;

    @InjectMocks
    private PacketWorkflowService packetWorkflowService;

    private ExamPacket packet;

    @BeforeEach
    void setUp() {
        Department department = Department.builder().deptId("D1").deptName("Computing").build();
        Course course = Course.builder().courseCode("CS101").courseName("Intro to CS").department(department).build();
        AcademicCycle cycle = AcademicCycle.builder().cycleId("2026-S1").build();

        Role lecturerRole = Role.builder().roleId("R1").roleName("Lecturer").build();
        Role moderatorRole = Role.builder().roleId("R2").roleName("Moderator").build();

        User currentHolder = User.builder().userId("U1").name("Dr. Lecturer").role(lecturerRole).build();
        User moderator = User.builder().userId("U2").name("Dr. Moderator").role(moderatorRole).build();

        packet = ExamPacket.builder()
                .packetId("P1")
                .course(course)
                .academicCycle(cycle)
                .status("PAPER_MARKING")
                .deadline(LocalDate.now().plusDays(2))
                .currentHolder(currentHolder)
                .build();

        PacketAssignment lecturerAssignment = PacketAssignment.builder()
                .assignmentId("A1")
                .packet(packet)
                .user(currentHolder)
                .assignedRole("LECTURER")
                .build();

        PacketAssignment moderatorAssignment = PacketAssignment.builder()
                .assignmentId("A2")
                .packet(packet)
                .user(moderator)
                .assignedRole("MODERATOR")
                .build();

        when(examPacketRepository.findAll()).thenReturn(List.of(packet));
        when(packetAssignmentRepository.findByPacketPacketId("P1"))
                .thenReturn(List.of(lecturerAssignment, moderatorAssignment));
        when(packetMovementRepository.findByPacketPacketIdOrderByTimestampDesc("P1"))
                .thenReturn(List.of());
    }

    @Test
    void filterAndSearchPackets_supportsPacketIdSearchAndMapsHodFields() {
        List<DepartmentPacketResponseDto> results = packetWorkflowService.filterAndSearchPackets("D1", "P1", null, null, null);

        assertThat(results).hasSize(1);
        DepartmentPacketResponseDto dto = results.get(0);
        assertThat(dto.getStage()).isEqualTo("PAPER_MARKING");
        assertThat(dto.getAcademicCycle()).isEqualTo("2026-S1");
        assertThat(dto.getLecturerId()).isEqualTo("U1");
        assertThat(dto.getModeratorName()).isEqualTo("Dr. Moderator");
    }
}
