package com.suppkart.dto.content;

import com.suppkart.model.enums.PageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private PageStatus status;
    private UserDTO lastUpdatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String name;
        private String email;
    }
}
