package com.gs.api.controller;

import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.domain.registration.Registration;
import com.gs.api.domain.registration.RegistrationResponse;
import com.gs.api.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RestController
public class UtilController extends BaseController {

    static final Logger logger = LoggerFactory.getLogger(UtilController.class);

    @Value("${property.name}")
    private String propertyName;


    @Autowired
    private EmailService emailService;


    /**
     * A simple "is alive" API.
     *
     * @return Empty response with HttpStatus of OK
     * @throws Exception
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> ping() throws Exception {
        logger.trace("Service ping initiated");
        try {
            //TODO REMOVE THIS ASHLEY
            Registration reg = new Registration();
            reg.setId("abc123");
            reg.setOrderNumber("123");
            reg.setSessionId("610076");
            reg.setStudentId("persn000000000595680");
            List<Registration> regs = new ArrayList<>();
            regs.add(reg);
            reg = new Registration();
            reg.setId("abc456");
            reg.setOrderNumber("456");
            reg.setSessionId("610076");
            reg.setStudentId("persn000000000535454");
            regs.add(reg);
            PaymentConfirmation pay = new PaymentConfirmation(new Payment(100.12, "auth1234", "ref123"), "sale123");

            List<PaymentConfirmation> pays = new ArrayList<>();
            pays.add(pay);
            pay = new PaymentConfirmation(new Payment(123.09, "auth5678", "ref123"), "sale123");
            pays.add(pay);
            RegistrationResponse regRes = new RegistrationResponse(regs, pays);
            String[] recipients = {"ashley.c.hope@gmail.com", "ahope@agiletrailblazers.com", "bademail@test.com"};
            emailService.sendPaymentReceiptEmail(recipients, regRes);
        }
        catch (Exception e) {

        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * A simple API to tell us which environment it is 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/env", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String env() throws Exception {
        logger.trace("Service env initiated");
        return propertyName;
    }
    
}
