package com.aditya.File.share.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreditsDTO {
    private Integer credits;
    private String  plan;
}
