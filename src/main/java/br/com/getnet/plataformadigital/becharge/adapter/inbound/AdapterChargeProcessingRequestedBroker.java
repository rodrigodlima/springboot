package br.com.getnet.plataformadigital.becharge.adapter.inbound;

import static org.apache.http.HttpStatus.SC_CONFLICT;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Headers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.getnet.plataformadigital.becharge.config.broker.BrokerInput;
import br.com.getnet.plataformadigital.becharge.core.ChargeCore;
import br.com.getnet.plataformadigital.becharge.domain.ChargeData;
import br.com.getnet.plataformadigital.becharge.domain.ChargeProcessingRequested;
import br.com.getnet.plataformadigital.commons.exceptions.ErrorDetail;

@EnableBinding(BrokerInput.class)
public class AdapterChargeProcessingRequestedBroker {
	
	private Logger log = LoggerFactory.getLogger(AdapterChargeProcessingRequestedBroker.class);
	private final ChargeCore chargeCore;
	
	public AdapterChargeProcessingRequestedBroker(ObjectMapper objectMapper, ChargeCore chargeCore) {
		this.chargeCore = chargeCore;
	}
	
	@StreamListener(target = BrokerInput.EXCHANGE_CHARGE_PROCESSING_REQUESTED)
	public void subscribeExchangeChargeProcessingRequested(ChargeProcessingRequested chargeProcessingRequested, @Headers Map<String, Object> headers) {
		String chargeId = chargeProcessingRequested.getChargeId();
		try {
			ChargeData chargeData = chargeCore.findOne(chargeId);
			
			if (!chargeData.sendToPaymentDataEnrichment()) {
				ErrorDetail detail = new ErrorDetail();
				detail.setCode(SC_CONFLICT);
				detail.setReason("Cobrança já está em processo de pagamento.");
				detail.setDescription("A cobrança está com a situação " + chargeData.getStatus() +
								", significando que a mesma já está em processo de pagamento.");
				
				chargeCore.sendMessageError(chargeProcessingRequested, detail, headers);
			} else {
				chargeCore.updateStatusToProcessing(chargeData);
				chargeCore.sendMessageForPaymentDataEnrichment(chargeData, headers);
			}
		} catch (Exception e) {
			log.error("Error ao processar a Charge [{}]. Ative o log de debug para mais detalhes [{}].", chargeId, e.getMessage());
			log.debug("Error ao processar a Charge [" + chargeId + "].", e);
			
			chargeCore.notifyApplicationError(chargeProcessingRequested, e, headers);
		}
	}
}
