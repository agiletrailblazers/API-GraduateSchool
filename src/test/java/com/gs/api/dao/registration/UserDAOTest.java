package com.gs.api.dao.registration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import com.gs.api.dao.registration.UserDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class UserDAOTest {

    @InjectMocks
    @Autowired
    private UserDAO userDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SimpleJdbcCall personInsertActor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInsertUser() throws Exception {
        Map<String, Object> expectedResults = new HashMap();
        expectedResults.put("testOutParam", 2);
        doReturn(expectedResults).when(personInsertActor).execute(any(SqlParameterSource.class));
        Map<String, Object> actualResults = userDAO.insertNewUser("test@test.gov", "firstName", "middle",
                "lastName", false, "Test Office", "123 Test Street", null, "testCity", "MD", "12345", null, null, null,
                null, null, null, "1234561234", null, "password1", null);

        assertNotNull(actualResults);
        assertEquals(expectedResults.get("testOutParam"), actualResults.get("testOutParam"));
    }

    @Test
    public void testFailToInsertUser() throws Exception {
        when(personInsertActor.execute(any(SqlParameterSource.class)))
                .thenThrow(new IllegalArgumentException("BAD SQL"));

        try {
            Map<String, Object> actualResults = userDAO.insertNewUser("test@test.gov", "firstName", "middle",
                    "lastName", false, "Test Office", "123 Test Street", null, "testCity", "MD", "12345", null, null, null,
                    null, null, null, "1234561234", null, "password1", null);
            assertTrue(false); //Should never reach this line
        }
        catch (IllegalArgumentException iE) {
            assertNotNull(iE);
            assertTrue(iE instanceof Exception);
        }

    }

    @Test
    public void testGetAccount() throws Exception {

    }

    @Test
    public void testFailToGetAccount() throws Exception {

    }
}
