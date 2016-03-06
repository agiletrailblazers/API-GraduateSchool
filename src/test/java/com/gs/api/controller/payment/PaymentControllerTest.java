package com.gs.api.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.payment.Payment;
import com.gs.api.exception.PaymentException;
import com.gs.api.service.authentication.AuthenticationService;
import com.gs.api.service.payment.PaymentService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:spring/test-root-context.xml"})
public class PaymentControllerTest {

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
        List<Payment> payments = Collections.singletonList(payment);
        String jsonModel = new ObjectMapper().writeValueAsString(payments);

        mockMvc.perform(post("/payment/reverse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isNoContent());

        verify(paymentService).reversePayment(capturedPayment.capture());

        Payment thePayment = capturedPayment.getValue();
        assertEquals(thePayment.getAmount(), PAYMENT_AMOUNT, 0.001);
        assertEquals(thePayment.getAuthorizationId(), AUTHORIZATION_ID);
        assertEquals(thePayment.getMerchantReferenceId(), MERCHANT_ID);
    }

    @Test
    public void testReversePayment_PaymentException() throws Exception {

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);
        List<Payment> payments = Collections.singletonList(payment);
        String jsonModel = new ObjectMapper().writeValueAsString(payments);

        PaymentException pe = new PaymentException("I made reversal fail");
        doThrow(pe).when(paymentService).reversePayment(any(Payment.class));

        mockMvc.perform(post("/payment/reverse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(is(pe.getMessage())));

        verify(paymentService).reversePayment(capturedPayment.capture());

        Payment thePayment = capturedPayment.getValue();
        assertEquals(thePayment.getAmount(), PAYMENT_AMOUNT, 0.001);
        assertEquals(thePayment.getAuthorizationId(), AUTHORIZATION_ID);
        assertEquals(thePayment.getMerchantReferenceId(), MERCHANT_ID);
    }

}
