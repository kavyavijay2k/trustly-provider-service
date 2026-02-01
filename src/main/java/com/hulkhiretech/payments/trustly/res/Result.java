package com.hulkhiretech.payments.trustly.res;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Result {

    @SerializedName("signature")
    private String signature;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("method")
    private String method;

    @SerializedName("data")
    private ResultData data;
}
