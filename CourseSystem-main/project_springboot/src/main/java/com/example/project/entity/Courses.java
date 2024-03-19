package com.example.project.entity;

import lombok.Data;

@Data
public class Courses {
    private Integer week_number;
    private Integer day_number;
    private Integer batch_number;
    private Integer capacity;
    private Integer selected_number; //已选人数
}

