package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoverennostList {
    private ErrorResponse errorResponse;
    @JsonDeserialize
    private List< Doverennost > doverennostsList;
}
