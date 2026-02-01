package com.hulkhiretech.payments.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.pojo.DepositRequest;
import com.hulkhiretech.payments.pojo.DepositResponse;
import com.hulkhiretech.payments.service.interfaces.DepositService;
import com.hulkhiretech.payments.trustly.req.Attributes;
import com.hulkhiretech.payments.trustly.req.DataNode;
import com.hulkhiretech.payments.trustly.req.DepositRequestWrapper;
import com.hulkhiretech.payments.trustly.req.Params;
import com.hulkhiretech.payments.trustly.res.TrustlyDepositResponse;
import com.hulkhiretech.payments.trustly.security.RS256SignerVerifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

private final HttpServiceEngine httpServiceEngine;
private final Gson gson;
@Value("${merchant.username}")
private String merchantUserName;
@Value("${merchant.password}")
private String merchantPassword;
@Value("${notification.Url}")
private String notificationUrl;
@Value("${trustly.Url}")
private String trustlyDepositUrl;
	@Override
	public DepositResponse createDeposit(DepositRequest depositRequest) {
		log.info("create new deposit");
		
		
		Attributes attributes = Attributes.builder()
				.country(depositRequest.getCountry())
				.locale(depositRequest.getLocale())
				.currency(depositRequest.getCurrency())
				.amount(depositRequest.getAmount())
				.firstName(depositRequest.getFirstName())
				.lastName(depositRequest.getLastName())
				.email(depositRequest.getEmail())
				.successUrl(depositRequest.getSuccessUrl())
				.failUrl(depositRequest.getFailUrl())
				.build();


				
				DataNode data = DataNode.builder()
				.username(merchantUserName)
				.password(merchantPassword)
				.notificationUrl(notificationUrl)
				.endUserId(depositRequest.getEndUserId())
				.messageId(depositRequest.getTxnReference())
				.attributes(attributes)
				.build();
				
				String signature=RS256SignerVerifier.sign(Constant.DEPOSIT, depositRequest.getTxnReference(), data);
				
				Params params = Params.builder()
						.signature(signature)
						.uuid(depositRequest.getTxnReference())
						.data(data)
						.build();


				DepositRequestWrapper request = DepositRequestWrapper.builder()
						.method(Constant.DEPOSIT)
						.params(params)
						.version(Constant.VERSION_1_1)
						.build();


						Gson gson = new GsonBuilder()
						.setPrettyPrinting()
						.create();


						String jsonReqData = gson.toJson(request);

		
		HttpRequest httpRequest=new HttpRequest();
		httpRequest.setBody(jsonReqData);
		httpRequest.setUrl(trustlyDepositUrl);
		httpRequest.setHttpMethod(HttpMethod.POST);
		ResponseEntity<String> httpResponse=httpServiceEngine.makeHttpCall(httpRequest);
		DepositResponse responseObj= processResponse(httpResponse);
		return responseObj;
	}
	private DepositResponse processResponse(ResponseEntity<String> httpResponse) {
		if(httpResponse.getStatusCode().is2xxSuccessful()) {
			String body=httpResponse.getBody();
			TrustlyDepositResponse trustlyDepositResponse =
					gson.fromJson(body, TrustlyDepositResponse.class);
			log.info("trustly parsed response {}",trustlyDepositResponse);
			if(trustlyDepositResponse!=null && trustlyDepositResponse.getResult()!=null && trustlyDepositResponse.getResult().getData()!=null && trustlyDepositResponse.getResult().getData().getUrl()!=null ) {
				DepositResponse response=new DepositResponse();
				response.setOrderid(trustlyDepositResponse.getResult().getData().getOrderId());
				response.setUrl(trustlyDepositResponse.getResult().getData().getUrl());
				log.info("response:{}",response);
				return response;
			}
			
			log.error("Deposit response is invalid");
		}
		return null  ;
		
	}

}
