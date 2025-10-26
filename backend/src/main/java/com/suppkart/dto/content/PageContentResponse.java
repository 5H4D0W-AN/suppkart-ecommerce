package com.suppkart.dto.content;

import com.suppkart.model.enums.PageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageContentResponse {
    private PageType pageType;
    private String pageDisplayName;
    private List<SeoMetadataDTO> elements;
    private Integer totalElements;
}