package com.aditya.File.share.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileMetaDataDTO {
    private String id;
    private String name;
    private Long size;
    private String type;
    private String clerkId;
    private Boolean isPublic;
    private String fileLocation;
    private String publicId;
    private LocalDateTime uploadAt;
}
