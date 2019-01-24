package br.com.getnet.plataformadigital.becharge.core;

import static br.com.getnet.plataformadigital.becharge.domain.Status.PROCESSING;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.getnet.plataformadigital.becharge.domain.ChargeData;
import br.com.getnet.plataformadigital.becharge.domain.ChargeProcessingRequested;
import br.com.getnet.plataformadigital.becharge.domain.PaymentDataEnrichmentRequested;
import br.com.getnet.plataformadigital.becharge.port.inbound.PortInbound;
import br.com.getnet.plataformadigital.becharge.port.outbound.PortOutbound;
import br.com.getnet.plataformadigital.becharge.repository.ChargeDataRepository;
import br.com.getnet.plataformadigital.commons.exceptions.DefaultError;
import br.com.getnet.plataformadigital.commons.exceptions.ErrorDetail;
import br.com.getnet.plataformadigital.commons.headers.HeaderHelper;

@Service
@Transactional
public class ChargeCore implements PortInbound {
	
	@Value("${app.rabbitmq.maximumNumberOfRetries}")
	private int maximumNumberOfRetries;

	private final PortOutbound portOutput;
	private final ChargeDataRepository chargeDataRepository;

	public ChargeCore(ChargeDataRepository chargeDataRepository, PortOutbound portOutput) {
		this.chargeDataRepository = chargeDataRepository;
		this.portOutput = portOutput;
	}
	
	@Override
	public ChargeData findOne(String chargeId) {
		return chargeDataRepository.findOne(chargeId);
	}

	@Override
	public ChargeData update(ChargeData chargeData) {
		return chargeDataRepository.save(chargeData);
	}
	
	@Override
	public ChargeData updateStatusToProcessing(ChargeData chargeData) {
		chargeData.setStatus(PROCESSING);
		return update(chargeData);
	}
	
	@Override
	public void sendMessageForPaymentDataEnrichment(ChargeData chargeData, Map<String, Object> headers) {
		PaymentDataEnrichmentRequested paymentDataEnrichmentRequested = new PaymentDataEnrichmentRequested();
		
		paymentDataEnrichmentRequested.setChargeId(chargeData.getChargeId());
		paymentDataEnrichmentRequested.setSellerId(chargeData.getSellerId());
		paymentDataEnrichmentRequested.setSubscriptionId(chargeData.getSubscriptionId());
		
		portOutput.sendMessageForPaymentDataEnrichment(paymentDataEnrichmentRequested, headers);
	}
	
	@Override
	public void sendMessageError(ChargeProcessingRequested originalMessage, ErrorDetail detail, Map<String, Object> headers) {
		portOutput.sendMessageError(new DefaultError(originalMessage).addDetail(detail), headers);
	}

	@Override
	public void notifyApplicationError(ChargeProcessingRequested chargeProcessingRequested, Exception e,
			Map<String, Object> headers) {
		if (HeaderHelper.getRetryCount(headers) < maximumNumberOfRetries) {
			portOutput.sendMessageForChargeProcessingRequestedWithWait(chargeProcessingRequested, headers);
		} else {
			ErrorDetail detail = new ErrorDetail();
			detail.setCode(SC_INTERNAL_SERVER_ERROR);
			detail.setReason("Ocorreu um erro inesperado.");
			detail.setDescription(e.getMessage());
			
			sendMessageError(chargeProcessingRequested, detail, headers);
		}
		
	}
	
}