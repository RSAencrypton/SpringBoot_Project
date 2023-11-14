package com.sky.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDish {

    private Long id;
    private Integer stock;
    private String description;
    private Integer payVal;
    private Integer actualVal;
    private Integer status;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
