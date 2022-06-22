package com.ssd.mvd.gpstabletsservice.payload;

import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import lombok.Data;

import java.util.List;

@Data
public class ReqExchangeLocation {
    private String token;
    private String passportSeries;
    private List< ReqLocationExchange >  reqLocationExchanges;

    public String getPassport () { return this.passportSeries != null ? this.passportSeries : ( this.passportSeries = RedisDataControl.getRedis().decode( token ) ); }
}
