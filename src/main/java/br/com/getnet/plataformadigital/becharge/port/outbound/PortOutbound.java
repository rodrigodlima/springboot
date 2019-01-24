package br.com.getnet.plataformadigital.becharge.port.outbound;

import java.util.Map;

import br.com.getnet.plataformadigital.becharge.domain.ChargeProcessingRequested;
import br.com.getnet.plataformadigital.becharge.domain.PaymentDataEnrichmentRequested;
import br.com.getnet.plataformadigital.commons.exceptions.DefaultError;

public interface PortOutbound {
	void sendMessageForPaymentDataEnrichment(PaymentDataEnrichmentRequested paymentDataEnrichmentRequested, Map<String, Object> headers);
	void sendMessageError(DefaultError error, Map<String, Object> headers);
	void sendMessageForChargeProcessingRequestedWithWait(ChargeProcessingRequested chargeProcessingRequested, Map<String, Object> headers);
}
