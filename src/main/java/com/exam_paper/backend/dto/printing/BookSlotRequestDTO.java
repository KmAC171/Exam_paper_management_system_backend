package com.exam_paper.backend.dto.printing;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSlotRequestDTO {
    private Long packetId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private Integer copies;
    private String notes;

    @JsonSetter("packetId")
    public void setPacketId(Object packetIdVal) {
        if (packetIdVal == null) {
            this.packetId = null;
        } else if (packetIdVal instanceof Number num) {
            this.packetId = num.longValue();
        } else {
            String str = packetIdVal.toString().trim();
            if (str.contains("-")) {
                String[] parts = str.split("-");
                try {
                    this.packetId = Long.parseLong(parts[parts.length - 1]);
                } catch (NumberFormatException e) {
                    this.packetId = null;
                }
            } else {
                try {
                    this.packetId = Long.parseLong(str);
                } catch (NumberFormatException e) {
                    this.packetId = null;
                }
            }
        }
    }
}
