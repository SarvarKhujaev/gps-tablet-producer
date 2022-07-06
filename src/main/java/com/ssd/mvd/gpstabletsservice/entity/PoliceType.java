package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PoliceType {
    private UUID uuid;
    private String policeType;
}
