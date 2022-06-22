package com.ssd.mvd.gpstabletsservice.request;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import lombok.Data;

import java.util.UUID;

@Data
public class SelfEmploymentRequest {
    private Patrul patrul;
    private UUID uuid;
}
