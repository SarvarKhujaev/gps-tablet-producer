package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import lombok.Data;
import java.util.Map;
import java.util.Date;

@Data
public class EventBody {
    private Integer camera;
    private Boolean matched;
    private Date created_date;
    private Double confidence;

    private String id;
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    private byte[] fullframebytes;
    private byte[] thumbnailbytes;

    private Map< String, String > headwear;
    private Map< String, String > top_color;
    private Map< String, String > bottom_color;
    private Map< String, String > lower_clothes;
    private Map< String, String > upper_clothes;
    private Map< String, String > detailed_upper_clothes;
}
