package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_CAR;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskOperations;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.VictimHumans;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class CarEvent extends TaskOperations {
    private String name;
    private String comment;
    private Double confidence;
    private String dossier_photo;

    @JsonProperty( value = "id" )
    private String id;

    @JsonProperty( value = "thumbnail" )
    private String thumbnail;

    @JsonProperty( value = "fullframe" )
    private String fullframe;

    @JsonProperty( value = "created_date" )
    private String created_date;

    @JsonDeserialize
    private DataInfo dataInfo;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_CAR,
            this.getId()
    );

    public CarEvent update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }

    @JsonDeserialize
    private CarTotalData carTotalData;

    @JsonDeserialize
    private List< VictimHumans > victimHumans;
}