package com.hulkhiretech.payments.trustly.res;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResultData {

    @SerializedName("orderid")
    private String orderId;

    @SerializedName("url")
    private String url;
}
