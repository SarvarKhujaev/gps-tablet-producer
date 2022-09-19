package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatrulStatus {
    private Patrul patrul;
    private Boolean inTime; // показывает пришел ли Патрульный вовремя
    private Long totalTimeConsumption; // показывает сколько времени Патрульный потратил на всю задачу от начала до конца
}
