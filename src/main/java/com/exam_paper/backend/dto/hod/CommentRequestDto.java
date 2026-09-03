package com.exam_paper.backend.dto.hod;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {
    private String packetId;
    private String userId;
    private String commentText;
}
