package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import lombok.Data;
import java.util.Map;
import java.util.Date;

@Data
public class EventFace {
    private Long age;
    private Integer camera;
    private Boolean matched;
    private Date created_date;
    private Double confidence;

    private byte[] fullframebytes;
    private byte[] thumbnailbytes;

    private String id;
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    private Map< String, String > beard;
    private Map< String, String > gender;
    private Map< String, String > glasses;
    private Map< String, String > medmask;
}
