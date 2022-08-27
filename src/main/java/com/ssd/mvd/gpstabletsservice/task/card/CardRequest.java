package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;

import lombok.extern.jackson.Jacksonized;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest< T > {
    private TaskTypes taskType;
    @JsonDeserialize
    private T card;
    @JsonDeserialize
    private List< UUID > patruls;
}
