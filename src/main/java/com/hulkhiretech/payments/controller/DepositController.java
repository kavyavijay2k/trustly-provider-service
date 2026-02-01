package com.hulkhiretech.payments.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.pojo.DepositRequest;
import com.hulkhiretech.payments.pojo.DepositResponse;
import com.hulkhiretech.payments.service.interfaces.DepositService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/trustly/deposits")
@Slf4j
@RequiredArgsConstructor
public class DepositController {
	
	private final DepositService depositService;
	@PostMapping
	public DepositResponse createDeposit(@RequestBody DepositRequest depositRequest) {
		log.info("request body :{}",depositRequest);
		
		DepositResponse response=depositService.createDeposit(depositRequest);
		log.info("creating a new deposit");
		return  response;
	}

}
