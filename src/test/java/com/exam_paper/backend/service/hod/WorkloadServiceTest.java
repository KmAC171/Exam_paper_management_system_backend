package com.exam_paper.backend.service.hod;

import com.exam_paper.backend.dto.hod.LecturerWorkloadDto;
import com.exam_paper.backend.entity.*;
import com.exam_paper.backend.repository.MarkingRepository;
import com.exam_paper.backend.repository.PacketAssignmentRepository;
import com.exam_paper.backend.repository.UserRepository;
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
class WorkloadServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MarkingRepository markingRepository;

    @Mock
    private PacketAssignmentRepository packetAssignmentRepository;

    @InjectMocks
    private WorkloadService workloadService;

    @Test
    void getDepartmentWorkload_populatesStageCountsAndOverdue() {
        Department department = Department.builder().deptId("D1").build();
        Role lecturerRole = Role.builder().roleId("R1").roleName("Lecturer").build();
        User lecturer = User.builder().userId("U1").name("Dr. Lecturer").department(department).role(lecturerRole).build();

        Course course = Course.builder().department(department).build();
        ExamPacket markingPacket = ExamPacket.builder()
                .packetId("P1")
                .course(course)
                .status("PAPER_MARKING")
                .deadline(LocalDate.now().plusDays(1))
                .build();
        ExamPacket completedPacket = ExamPacket.builder()
                .packetId("P2")
                .course(course)
                .status("COMPLETED")
                .deadline(LocalDate.now().minusDays(1))
                .build();
        ExamPacket overduePacket = ExamPacket.builder()
                .packetId("P3")
                .course(course)
                .status("PAPER_SETTING")
                .deadline(LocalDate.now().minusDays(2))
                .build();

        PacketAssignment a1 = PacketAssignment.builder().packet(markingPacket).user(lecturer).build();
        PacketAssignment a2 = PacketAssignment.builder().packet(completedPacket).user(lecturer).build();
        PacketAssignment a3 = PacketAssignment.builder().packet(overduePacket).user(lecturer).build();

        Marking marking = Marking.builder().lecturer(lecturer).totalScripts(100).markedScripts(40).build();

        when(userRepository.findAll()).thenReturn(List.of(lecturer));
        when(packetAssignmentRepository.findByUserUserId("U1")).thenReturn(List.of(a1, a2, a3));
        when(markingRepository.findAll()).thenReturn(List.of(marking));

        List<LecturerWorkloadDto> result = workloadService.getDepartmentWorkload("D1");

        assertThat(result).hasSize(1);
        LecturerWorkloadDto dto = result.get(0);
        assertThat(dto.getMarking()).isEqualTo(1);
        assertThat(dto.getPaperSetting()).isEqualTo(1);
        assertThat(dto.getCompleted()).isEqualTo(1);
        assertThat(dto.getOverdue()).isEqualTo(1);
        assertThat(dto.getTotalAssignedPackets()).isEqualTo(3);
        assertThat(dto.getProgressPercentage()).isEqualTo(40.0);
    }
}
