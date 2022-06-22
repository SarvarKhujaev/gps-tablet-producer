package com.ssd.mvd.gpstracker.request;

import com.ssd.mvd.gpstracker.entity.Patrul;
import lombok.Data;

import java.util.UUID;

@Data
public class SelfEmploymentRequest {
    private Patrul patrul;
    private UUID uuid;
}
