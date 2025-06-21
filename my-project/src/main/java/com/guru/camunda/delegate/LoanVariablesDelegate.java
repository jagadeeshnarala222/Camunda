package com.guru.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class LoanVariablesDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
		Integer amount = (Integer) execution.getVariable("amount");
		execution.setVariable("approved", amount<1000);
	}

}
