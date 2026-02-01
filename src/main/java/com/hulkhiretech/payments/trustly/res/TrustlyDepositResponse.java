package com.hulkhiretech.payments.trustly.res;



import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class TrustlyDepositResponse {
	
	

	@SerializedName("result")
	private Result result;


	@SerializedName("version")
	private String version;
	

}
