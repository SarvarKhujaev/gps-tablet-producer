package com.ssd.mvd.gpstabletsservice.request;

import java.util.UUID;

@lombok.Data // используется когда патрульный хочвте поменять свое фото
public class PatrulImageRequest {
    private String newImage;
    private UUID patrulUUID;
}
