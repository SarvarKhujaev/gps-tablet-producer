package com.ssd.mvd.gpstracker.payload;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import lombok.Data;

import java.util.List;

@Data
public class ReqExchangeLocation {
    private String token;
    private String passportSeries;
    private List< ReqLocationExchange >  reqLocationExchanges;

    public String getPassport () { return this.passportSeries != null ? this.passportSeries : ( this.passportSeries = RedisDataControl.getRedis().decode( token ) ); }
}
