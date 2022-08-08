package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoverennostList {
    @JsonDeserialize
    private List< Doverennost > doverennostsList;
}
