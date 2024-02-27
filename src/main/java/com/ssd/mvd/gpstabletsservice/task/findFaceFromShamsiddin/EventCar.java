package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskOperations;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_EVENT_CAR;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class EventCar extends TaskOperations {
    private Status status = Status.CREATED;
    private Boolean matched;
    private Date created_date;
    private Double confidence;

    private String id;
    private String fullframe;
    private String thumbnail;
    private String dossier_photo;

    @JsonDeserialize
    private CarTotalData carTotalData;

    @JsonDeserialize
    private DataInfo dataInfo;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_EVENT_CAR,
            this.getId()
    );

    public EventCar update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }
}
