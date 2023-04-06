package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;

import lombok.extern.jackson.Jacksonized;
import java.util.List;
import java.util.UUID;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class CardRequest< T > {
    private TaskTypes taskType;
    @JsonDeserialize
    private T card;
    @JsonDeserialize
    private List< UUID > patruls;
}
