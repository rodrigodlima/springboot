package br.com.getnet.plataformadigital.becharge.domain;

import static br.com.getnet.plataformadigital.becharge.domain.Status.CANCELED;
import static br.com.getnet.plataformadigital.becharge.domain.Status.DENIED;
import static br.com.getnet.plataformadigital.becharge.domain.Status.PAID;
import static br.com.getnet.plataformadigital.becharge.domain.Status.PAYMENT_PENDING;
import static br.com.getnet.plataformadigital.becharge.domain.Status.PROCESSING;
import static br.com.getnet.plataformadigital.becharge.domain.Status.SCHEDULED;
import static br.com.getnet.plataformadigital.becharge.domain.Status.UNDO_FAILED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import br.com.getnet.plataformadigital.becharge.domain.ChargeData;

public class ChargeDataTest {

	private ChargeData chargeData;

	@Before
	public void setup() {
		chargeData = new ChargeData();
	}

	@Test
	public void testSendToEnrichPaymentDataWithScheduledStatus() {
		chargeData.setStatus(SCHEDULED);
		assertTrue(chargeData.sendToPaymentDataEnrichment());
	}

	@Test
	public void testSendToEnrichPaymentDataWithPaymentPendingStatus() {
		chargeData.setStatus(PAYMENT_PENDING);
		assertTrue(chargeData.sendToPaymentDataEnrichment());
	}

	@Test
	public void testSendToEnrichPaymentDataWithDeniedStatus() {
		chargeData.setStatus(DENIED);
		assertTrue(chargeData.sendToPaymentDataEnrichment());
	}

	@Test
	public void testSendToEnrichPaymentDataWithProcessingStatus() {
		chargeData.setStatus(PROCESSING);
		assertFalse(chargeData.sendToPaymentDataEnrichment());
	}

	@Test
	public void testSendToEnrichPaymentDataWithPaidStatus() {
		chargeData.setStatus(PAID);
		assertFalse(chargeData.sendToPaymentDataEnrichment());
	}

	@Test
	public void testSendToEnrichPaymentDataWithCanceledStatus() {
		chargeData.setStatus(CANCELED);
		assertFalse(chargeData.sendToPaymentDataEnrichment());
	}

	@Test
	public void testSendToEnrichPaymentDataWithUndoFailedStatus() {
		chargeData.setStatus(UNDO_FAILED);
		assertFalse(chargeData.sendToPaymentDataEnrichment());
	}
}
