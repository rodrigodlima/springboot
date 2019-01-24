package br.com.getnet.plataformadigital.becharge.config.broker;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public interface BrokerOutput {

	String PUBLISH_CHARGE_OPERATION_ERROR = "publishChargeOperationError";
	String PUBLISH_PAYMENT_DATA_ENRICHMENT_REQUESTED = "publishPaymentDataEnrichmentRequested";
	String PUBLISH_CHARGE_PROCESSING_REQUESTED_WITH_WAIT = "publishChargeProcessingRequestedWithWait";
	
	@Output(BrokerOutput.PUBLISH_PAYMENT_DATA_ENRICHMENT_REQUESTED)
	MessageChannel publishPaymentDataEnrichmentRequested();
	
	@Output(BrokerOutput.PUBLISH_CHARGE_OPERATION_ERROR)
	MessageChannel publishChargeOperationError();
	
	@Output(BrokerOutput.PUBLISH_CHARGE_PROCESSING_REQUESTED_WITH_WAIT)
	MessageChannel publishChargeProcessingRequestedWithWait();
}
