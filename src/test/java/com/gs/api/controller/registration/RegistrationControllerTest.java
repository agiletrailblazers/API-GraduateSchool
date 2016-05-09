package com.gs.api.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.api.domain.Address;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationDetails;
import com.gs.api.domain.registration.RegistrationRequest;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.exception.AuthenticationException;
import com.gs.api.exception.PaymentAcceptedException;
import com.gs.api.exception.PaymentDeclinedException;
import com.gs.api.exception.PaymentException;
import com.gs.api.service.authentication.AuthenticationService;
import com.gs.api.service.registration.RegistrationService;

import jdk.nashorn.internal.parser.JSONParser;
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

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class RegistrationControllerTest {
    private static final String SALE_ID = "saleId12345";
    public static final String ORDER_NUMBER = "23456";

    private static final String USER_ID = "person654321";
    private static final String SESSION_ID = "session654321";
    private static final String COURSE_NO = "course1234";
    private static final String COURSE_TITLE = "Introduction to Testing";
    private static final String REGISTRATION_ID = "12345";

    private static final double PAYMENT_AMOUNT = 0.00;
    private static final String AUTHORIZATION_ID = "1234";
    private static final String MERCHANT_ID = "5678";

    private static final Date START_DATE = new Date();
    private static final Date END_DATE = new Date(START_DATE.getTime() + 1000);
    private static final String TYPE = "CLASSROOM";
    private static final String ADDRESS1 = "123 Main Street";
    private static final String ADDRESS2 = "Suite 100";
    private static final String CITY = "Washington";
    private static final String STATE = "DC";
    private static final String ZIP = "12345";

    private Address address;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private AuthenticationService authenticationService;

    @Autowired
    @InjectMocks
    private RegistrationController registrationController;

    @Captor
    private ArgumentCaptor<RegistrationRequest> capturedRegistrations;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        MockitoAnnotations.initMocks(this);

        address = new Address();
        address.setAddress1(ADDRESS1);
        address.setAddress2(ADDRESS2);
        address.setCity(CITY);
        address.setState(STATE);
        address.setPostalCode(ZIP);
    }

    @Test
    public void testCreateRegistration() throws Exception {

        Registration registration = new Registration();
        registration.setStudentId(USER_ID);
        registration.setSessionId(SESSION_ID);
        List<Registration> registrations = Collections.singletonList(registration);

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);
        List<Payment> payments = Collections.singletonList(payment);
        RegistrationRequest registrationRequest = new RegistrationRequest(registrations, payments);

        Registration createdRegistration = new Registration();
        createdRegistration.setId(REGISTRATION_ID);
        List<Registration> createdRegistrations = Collections.singletonList(createdRegistration);

        List<PaymentConfirmation> paymentConfirmations = new ArrayList<>();
        for (Payment p : payments) {
            paymentConfirmations.add(new PaymentConfirmation(p, SALE_ID));
        }

        RegistrationResponse createdRegistrationResponse = new RegistrationResponse(createdRegistrations, paymentConfirmations);

        String jsonModel = new ObjectMapper().writeValueAsString(registrationRequest);

        when(registrationService.register(eq(USER_ID), isA(RegistrationRequest.class))).thenReturn(createdRegistrationResponse);

        mockMvc.perform(post("/registrations/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrations", hasSize(1)))
                .andExpect(jsonPath("$.registrations[0].id", is(REGISTRATION_ID)))
                .andExpect(jsonPath("$.paymentConfirmations", hasSize(1)))
                .andExpect(jsonPath("$.paymentConfirmations[0].saleId", is(SALE_ID)))
                .andExpect(jsonPath("$.paymentConfirmations[0].payment.authorizationId", is(AUTHORIZATION_ID)));

        verify(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));
        verify(registrationService).register(eq(USER_ID), capturedRegistrations.capture());
        assertEquals("Wrong user id", USER_ID, capturedRegistrations.getValue().getRegistrations().get(0).getStudentId());
        assertEquals("Wrong session id", SESSION_ID, capturedRegistrations.getValue().getRegistrations().get(0).getSessionId());
     }

    @Test
    public void testCreateRegistration_PaymentException() throws Exception {

        Registration registration = new Registration();
        registration.setStudentId(USER_ID);
        registration.setSessionId(SESSION_ID);
        List<Registration> registrations = Collections.singletonList(registration);

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);
        List<Payment> payments = Collections.singletonList(payment);
        RegistrationRequest registrationRequest = new RegistrationRequest(registrations, payments);

        List<PaymentConfirmation> paymentConfirmations = new ArrayList<>();
        for (Payment p : payments){
            paymentConfirmations.add(new PaymentConfirmation(p,null));
        }

        String jsonModel = new ObjectMapper().writeValueAsString(registrationRequest);

        PaymentException pe = new PaymentException("I made payment fail");
        when(registrationService.register(eq(USER_ID), isA(RegistrationRequest.class))).thenThrow(pe);

        mockMvc.perform(post("/registrations/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(is(pe.getMessage())));
    }

    @Test
    public void testCreateRegistration_PaymentDeclinedException() throws Exception {

        Registration registration = new Registration();
        registration.setStudentId(USER_ID);
        registration.setSessionId(SESSION_ID);
        List<Registration> registrations = Collections.singletonList(registration);

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);
        List<Payment> payments = Collections.singletonList(payment);
        RegistrationRequest registrationRequest = new RegistrationRequest(registrations, payments);

        List<PaymentConfirmation> paymentConfirmations = new ArrayList<>();
        for (Payment p : payments){
            paymentConfirmations.add(new PaymentConfirmation(p,null));
        }

        String jsonModel = new ObjectMapper().writeValueAsString(registrationRequest);

        PaymentDeclinedException pe = new PaymentDeclinedException("I made payment fail");
        when(registrationService.register(eq(USER_ID), isA(RegistrationRequest.class))).thenThrow(pe);

        mockMvc.perform(post("/registrations/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.message").value(is(pe.getMessage())));
    }

    @Test
    public void testCreateRegistration_PaymentAcceptedException() throws Exception {

        Registration registration = new Registration();
        registration.setStudentId(USER_ID);
        registration.setSessionId(SESSION_ID);
        List<Registration> registrations = Collections.singletonList(registration);

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);
        List<Payment> payments = Collections.singletonList(payment);
        RegistrationRequest registrationRequest = new RegistrationRequest(registrations, payments);

        List<PaymentConfirmation> paymentConfirmations = new ArrayList<>();
        for (Payment p : payments){
            paymentConfirmations.add(new PaymentConfirmation(p,null));
        }

        String jsonModel = new ObjectMapper().writeValueAsString(registrationRequest);

        PaymentAcceptedException pe = new PaymentAcceptedException("I made payment fail");
        when(registrationService.register(eq(USER_ID), isA(RegistrationRequest.class))).thenThrow(pe);

        mockMvc.perform(post("/registrations/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(is(pe.getMessage())));
    }

    @Test
    public void testCreateRegistration_WrongUser() throws Exception {

        Registration registration = new Registration();
        registration.setStudentId(USER_ID);
        registration.setSessionId(SESSION_ID);
        List<Registration> registrations = Collections.singletonList(registration);

        Payment payment = new Payment(PAYMENT_AMOUNT, AUTHORIZATION_ID, MERCHANT_ID);
        List<Payment> payments = Collections.singletonList(payment);
        RegistrationRequest registrationRequest = new RegistrationRequest(registrations, payments);

        List<PaymentConfirmation> paymentConfirmations = new ArrayList<>();
        for (Payment p : payments){
            paymentConfirmations.add(new PaymentConfirmation(p,null));
        }

        String jsonModel = new ObjectMapper().writeValueAsString(registrationRequest);

        doThrow(new AuthenticationException("test user is a fraud")).when(authenticationService).verifyUser(isA(HttpServletRequest.class), eq(USER_ID));

        mockMvc.perform(post("/registrations/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonModel))
                .andExpect(status().isUnauthorized());

        verifyZeroInteractions(registrationService);
    }

    @Test
    public void testGetRegistration() throws Exception {
        Registration createdRegistration = new Registration();
        createdRegistration.setId(REGISTRATION_ID);
        createdRegistration.setSessionId(SESSION_ID);
        createdRegistration.setOrderNumber(ORDER_NUMBER);
        createdRegistration.setStudentId(USER_ID);

        when(registrationService.getRegistrationForSession(eq(USER_ID), eq(SESSION_ID))).thenReturn(Arrays.asList(createdRegistration));

        mockMvc.perform(get("/registrations/users/" + USER_ID + "/sessions/" + SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(REGISTRATION_ID)))
                .andExpect(jsonPath("$.[0].orderNumber", is(ORDER_NUMBER)))
                .andExpect(jsonPath("$.[0].studentId", is(USER_ID)))
                .andExpect(jsonPath("$.[0].sessionId", is(SESSION_ID)));

        verify(registrationService).getRegistrationForSession(eq(USER_ID), eq(SESSION_ID));
        assertEquals("Wrong user id", USER_ID, createdRegistration.getStudentId());
        assertEquals("Wrong session id", SESSION_ID, createdRegistration.getSessionId());
        assertEquals("Wrong order number", ORDER_NUMBER, createdRegistration.getOrderNumber());
        assertEquals("Wrong registration id", REGISTRATION_ID, createdRegistration.getId());
    }

    @Test
    public void testGetRegistrationDetails() throws Exception {
        RegistrationDetails registrationDetails = new RegistrationDetails(
                SESSION_ID,
                COURSE_NO,
                COURSE_TITLE,
                START_DATE.getTime(),
                END_DATE.getTime(),
                address,
                TYPE
        );

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(registrationDetails);

        List<RegistrationDetails> registrationDetailsList = Collections.singletonList(registrationDetails);

        when(registrationService.getRegistrationDetails(eq(USER_ID))).thenReturn(registrationDetailsList);

        mockMvc.perform(get("/registrations/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].sessionNo", is(SESSION_ID)))
                .andExpect(jsonPath("$.[0].courseNo", is(COURSE_NO)))
                .andExpect(jsonPath("$.[0].courseTitle", is(COURSE_TITLE)))
                .andExpect(jsonPath("$.[0].startDate", is(START_DATE.getTime())))
                .andExpect(jsonPath("$.[0].endDate", is(END_DATE.getTime())))
                .andExpect(jsonPath("$.[0].address.address1", is(ADDRESS1)))
                .andExpect(jsonPath("$.[0].address.address2", is(ADDRESS2)))
                .andExpect(jsonPath("$.[0].address.city", is(CITY)))
                .andExpect(jsonPath("$.[0].address.state", is(STATE)))
                .andExpect(jsonPath("$.[0].address.postalCode", is(ZIP)))
                .andExpect(jsonPath("$.[0].type", is(TYPE)));
    }

}
