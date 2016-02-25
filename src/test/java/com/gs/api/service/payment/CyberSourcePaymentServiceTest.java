package com.gs.api.service.payment;

import com.cybersource.ws.client.Client;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.exception.PaymentException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Properties;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Client.class)
public class CyberSourcePaymentServiceTest {

    private static final double AMOUNT = 149.50;
    private static final String AUTHORIZATION_ID = "authId12345";
    private static final String MERCHANT_REFERENCE_ID = "merch12345";
    static final String SALE_ID = "sale12345";

    private CyberSourcePaymentServiceImpl paymentService;

    @Captor
    private ArgumentCaptor<HashMap<String, String>> requestCaptor;

    @Captor
    private ArgumentCaptor<Properties> propertiesCaptor;

    /*
        By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
        but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        paymentService = new CyberSourcePaymentServiceImpl();

        mockStatic(Client.class);
    }

    @Test
    public void testProcessPayment_Success() throws Exception {

        final HashMap<String, String> response = new HashMap<>();
        response.put(CyberSourcePaymentServiceImpl.REQUEST_ID, SALE_ID);

        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenReturn(response);

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        PaymentConfirmation confirmation = paymentService.processPayment(payment);

        assertSame("wrong payment", payment, confirmation.getPayment());
        assertEquals("wrong sale transaction id", SALE_ID, confirmation.getTransactionID());

        PowerMockito.verifyStatic();
        Client.runTransaction(requestCaptor.capture(), propertiesCaptor.capture());
        assertEquals(5, requestCaptor.getValue().size());
        assertTrue(propertiesCaptor.getValue().isEmpty());

        // TODO verify correct request fields passed and correct client properties
     }

    @Test
    public void testProcessPayment_Failure() throws Exception {

        // setup expected exception
        IllegalArgumentException cause = new IllegalArgumentException("I caused test to fail");

        thrown.expect(PaymentException.class);
        thrown.expectMessage(CyberSourcePaymentServiceImpl.FAILED_TO_COMPLETE_SALE_MSG);
        thrown.expectCause(Matchers.is(cause));

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenThrow(cause);

        paymentService.processPayment(payment);
    }

}
