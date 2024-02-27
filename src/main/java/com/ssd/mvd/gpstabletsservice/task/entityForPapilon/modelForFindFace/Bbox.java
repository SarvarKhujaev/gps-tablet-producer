package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bbox {
    private Integer top;
    private Integer left;
    private Integer right;
    private Integer bottom;
}