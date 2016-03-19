package com.gs.api.search.util;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class TitleBuilderTest {

    private static final String URL_PDF = "http://assets.contentful.com/6s6s66s6s66s6s66s/page.pdf";
    private static final String URL_PDF_TRUNCATED = "page.pdf";
    private static final String URL_DOC = "http://assets.contentful.com/6s6s66s6s66s6s66s/page.docx";
    private static final String URL_DOC_TRUNCATED = "page.docx";
    private static final String URL_HTML = "http://www.graduateschool.edu/content/page.html";
    private static final String BODY = "This is some body content that is pretty long for testing";
    private static final String BODY_TRUNCATED = "This is some body co";
    private static final String TITLE = "This is a title";
    
    @InjectMocks
    @Autowired
    private TitleBuilder titleBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void buildTitle_Html_HasTitle() {
        String title = titleBuilder.parseTitle(TITLE, BODY, URL_HTML);
        assertEquals(TITLE, title);
    }
    
    @Test
    public void buildTitle_Html_HasBlankTitle() {
        String title = titleBuilder.parseTitle("", BODY, URL_HTML);
        assertEquals(BODY_TRUNCATED, title);
        title = titleBuilder.parseTitle(null, BODY, URL_HTML);
        assertEquals(BODY_TRUNCATED, title);
    }
    
    @Test
    public void buildTitle_Pdf_HasTitle() {
        String title = titleBuilder.parseTitle(TITLE, BODY, URL_PDF);
        assertEquals(TITLE, title);
    }
    
    @Test
    public void buildTitle_Pdf_HasBlankTitle() {
        String title = titleBuilder.parseTitle("", BODY, URL_PDF);
        assertEquals(URL_PDF_TRUNCATED, title);
        title = titleBuilder.parseTitle(null, BODY, URL_PDF);
        assertEquals(URL_PDF_TRUNCATED, title);
    }
    
    @Test
    public void buildTitle_WordDoc_HasTitle() {
        String title = titleBuilder.parseTitle(TITLE, BODY, URL_DOC);
        assertEquals(TITLE, title);
    }
    
    @Test
    public void buildTitle_WordDoc_HasBlankTitle() {
        String title = titleBuilder.parseTitle("", BODY, URL_DOC);
        assertEquals(URL_DOC_TRUNCATED, title);
        title = titleBuilder.parseTitle(null, BODY, URL_DOC);
        assertEquals(URL_DOC_TRUNCATED, title);
    }

}

