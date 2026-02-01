package com.hulkhiretech.payments.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositRequest {
	private String txnReference;
	private String endUserId;
	
	private String currency;
	private String firstName;
	private String lastName;
	private String country;
	private String locale;
	
	private String successUrl;
	private String failUrl;
	
	private String email;
	private Double amount;
	
}
