package br.com.getnet.plataformadigital.becharge.port.inbound;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import br.com.getnet.plataformadigital.becharge.domain.ChargeData;
import br.com.getnet.plataformadigital.becharge.domain.ChargeProcessingRequested;
import br.com.getnet.plataformadigital.commons.exceptions.ErrorDetail;

public interface PortInbound {
	ChargeData findOne(@NotBlank String chargeId); 
	ChargeData update(@NotNull ChargeData chargeData);
	ChargeData updateStatusToProcessing(@NotNull ChargeData chargeData);
	void sendMessageForPaymentDataEnrichment(@NotNull ChargeData chargeData, Map<String, Object> headers);
	void sendMessageError(ChargeProcessingRequested originalMessage, @NotNull ErrorDetail detail, Map<String, Object> headers);
	void notifyApplicationError(ChargeProcessingRequested chargeProcessingRequested, Exception e, Map<String, Object> headers);
}