package br.com.getnet.plataformadigital.becharge.config.broker;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface BrokerInput {
	
	String EXCHANGE_CHARGE_PROCESSING_REQUESTED = "subscribeChargeProcessingRequested";
	
	@Input(BrokerInput.EXCHANGE_CHARGE_PROCESSING_REQUESTED)
	SubscribableChannel subscribeChargeProcessingRequested();
}
