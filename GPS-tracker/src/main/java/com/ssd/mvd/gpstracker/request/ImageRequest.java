package com.ssd.mvd.gpstracker.request;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.Date;

@Data
@Builder
public class ImageRequest { // SAM - 91
    private Date dateOfCreation;
    private final byte[] bytes;

    private final String token;
    private String path;

    public Mono< String > getAbsolutePath () { return RedisDataControl.getRedis().getPatrul( RedisDataControl.getRedis().decode( this.getToken() ) ).map( patrul -> ( this.path = "/home/sarvar/Загрузки/MobileApp/src/main/resources/images/" + this.getPath() + "/" + patrul.getPassportNumber() + "_" + ( this.dateOfCreation = new Date() ) ) ); }
}
