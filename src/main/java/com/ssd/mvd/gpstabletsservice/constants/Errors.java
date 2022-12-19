package com.ssd.mvd.gpstabletsservice.constants;

public enum Errors {
    WRONG_PARAMS,
    DATA_NOT_FOUND,
    GAI_TOKEN_ERROR, // used when GAI token is invalid,
    SERVICE_WORK_ERROR,
    EXTERNAL_SERVICE_500_ERROR, // used when some service returns an error
}
