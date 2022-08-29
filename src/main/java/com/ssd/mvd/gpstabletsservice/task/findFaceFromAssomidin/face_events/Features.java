package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Features {
    @JsonDeserialize
    @SerializedName("gender")
    private Gender gender;

    @JsonDeserialize
    @SerializedName("age")
    private Age age;

    @JsonDeserialize
    @SerializedName("emotions")
    private Emotions emotions;

    @JsonDeserialize
    @SerializedName("beard")
    private Beard beard;

    @JsonDeserialize
    @SerializedName("glasses")
    private Glasses glasses;

    @JsonDeserialize
    @SerializedName("medmask")
    private Medmask medmask;
}