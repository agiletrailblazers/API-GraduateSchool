package com.gs.api.service;

import com.gs.api.dao.CommonDAO;
import com.gs.api.domain.registration.Timezone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class CommonServiceTest {

    @Mock
    private CommonDAO commonDAO;


    @InjectMocks
    private CommonServiceImpl commonService;

    /*
    By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
    but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetTimezones() throws Exception {
        Timezone expectedTimezone = new Timezone();
        expectedTimezone.setId("tmz123");
        expectedTimezone.setName("Easternish");
        List<Timezone> expectedList  = new ArrayList<>();
        expectedList.add(expectedTimezone);
        when(commonDAO.getTimezones()).thenReturn(expectedList);
        List<Timezone> returnedTimezones = commonService.getTimezones();
        assertEquals(1, returnedTimezones.size());
        assertEquals(returnedTimezones.get(0).getId(), expectedTimezone.getId());
        assertEquals(returnedTimezones.get(0).getName(), expectedTimezone.getName());

    }

    @Test
    public void testGetTimezones_RuntimeException() throws Exception {

        final RuntimeException expectedException = new RuntimeException("random exception");
        when(commonDAO.getTimezones()).thenThrow(expectedException);

        // setup expected exception
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(expectedException.getMessage());

        commonService.getTimezones();
    }
}
