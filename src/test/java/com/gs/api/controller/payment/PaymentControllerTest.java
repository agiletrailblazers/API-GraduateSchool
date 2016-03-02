package com.gs.api.controller.payment;

import com.gs.api.domain.payment.Payment;
import com.gs.api.service.authentication.AuthenticationService;
import com.gs.api.service.payment.PaymentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:spring/test-root-context.xml"})
public class PaymentControllerTest {
    final Logger logger = LoggerFactory.getLogger(PaymentControllerTest.class);

    private static final double PAYMENT_AMOUNT = 0.00;
    private static final String AUTHORIZATION_ID = "1234";
    private static final String MERCHANT_ID = "5678";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private PaymentService paymentService;

    @Mock
    private AuthenticationService authenticationService;

    @Autowired
    @InjectMocks
    private PaymentController paymentController;

    @Captor
    private ArgumentCaptor<Payment> capturedPayment;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReversePayment() throws Exception {

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);

        paymentService.reversePayment(payment);

        verify(paymentService).reversePayment(capturedPayment.capture());
        Payment thePayment = capturedPayment.getValue();

        assertEquals(thePayment.getAmount(), PAYMENT_AMOUNT, 0.001);
        assertEquals(thePayment.getAuthorizationId(), AUTHORIZATION_ID);
        assertEquals(thePayment.getMerchantReferenceId(), MERCHANT_ID);


    }

}
