package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

@Data
public class PatrulEntity {
    private String address;
    private UUID patrulUUID;
    private Date sosWasSendDate; // созраняет время когда запрос был отправлен

    private Double latitude;
    private Double longitude;

    private Map< UUID, Status > linkedPatrulList = new HashMap<>(); // список патрульных которые были назначены на это задание
    private Map< UUID, PatrulStatus > patrulStatuses = new HashMap<>();
}
