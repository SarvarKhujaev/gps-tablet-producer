package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskOperations;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class EventFace extends TaskOperations {
    private Long age;
    private Integer camera;
    private Boolean matched;
    private Date created_date;

    private Double latitude;
    private Double longitude;
    private Double confidence;

    private String id;
    private String address; // coming from front end
    private String cameraIp; // coming from front end
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_EVENT_FACE,
            this.getId()
    );

    public EventFace update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }
}
