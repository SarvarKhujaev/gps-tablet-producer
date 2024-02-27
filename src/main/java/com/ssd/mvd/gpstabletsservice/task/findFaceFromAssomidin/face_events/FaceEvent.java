package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_PERSON;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
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
public final class FaceEvent extends TaskOperations {
    private String name;
    private String comment; // Ф.И.О
    private String dossier_photo;

    private Double confidence;

    @JsonProperty( "id" )
    private String id;

    @JsonProperty( "created_date" )
    private String created_date;

    @JsonProperty( "thumbnail" )
    private String thumbnail;

    @JsonProperty( "fullframe" )
    private String fullframe;

    @JsonDeserialize
    private DataInfo dataInfo;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_PERSON,
            this.getId()
    );

    public FaceEvent update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    @JsonDeserialize
    private List<VictimHumans> victimHumans;
}