package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SosMessageForTopic {
    private UUID patrulUUID;
    private Boolean sosStatus; // показывает послал ли патрульный сос сигнал
}
