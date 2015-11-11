package com.gs.api.search.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;

import com.gs.api.search.util.SearchUrlBuilder;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-root-context.xml" })
public class SearchUrlBuilderTest {
    
    @Value("${search.solr.endpoint}")
    private String searchSolrEndpoint;

    @Value("${site.search.solr.query}")
    private String siteSearchSolrQuery;

    @Value("${course.search.solr.query}")
    private String courseSearchSolrQuery;

    @Mock
    private RestOperations restTemplate;

    @InjectMocks
    @Autowired
    private SearchUrlBuilder searchServiceHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void buildSiteSearchString() {
        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=(title:(*governnment*)) AND (content:(*governnment*))&start=0&rows=100&wt=json";
        String endpoint = searchServiceHelper.build(siteSearchSolrQuery,"governnment", 1, 100,new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=(title:(*governnment*)) AND (content:(*governnment*))&start=0&rows=100&wt=json";
        endpoint = searchServiceHelper.build(siteSearchSolrQuery,"governnment",1, 100,new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildCourseSearchString() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100,new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*Project Management*))^3 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*)) OR (course_abstract:(*Project Management*)) OR (course_prerequisites:(*Project Management*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"Project Management",1, 100,new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildSearchStringWithFacetParam() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&sort={sort}";
        String[] facetParam = {"city_state:Washington"};
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetParam);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*Project Management*))^3 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*)) OR (course_abstract:(*Project Management*)) OR (course_prerequisites:(*Project Management*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&fq=status:\"S\"&sort={sort}";
        String[] facetParams = {"city_state:Washington","status:S"};
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"Project Management",1, 100,facetParams);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void testStripAndEncode() {
        String result = searchServiceHelper.stripAndEncode("#&^%+-||!(){}[]\"~*?:\\");
        assertEquals("\\+\\-\\||\\!\\(\\)\\{\\}\\[\\]\\\"\\~\\*\\?\\:\\\\", result);
    }    

    @Test
    public void buildSiteSearchString_CurrentPage_EqualstoZero() {
        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=(title:(*governnment*)) AND (content:(*governnment*))&start=0&rows=100&wt=json";
        String endpoint = searchServiceHelper.build(siteSearchSolrQuery, "governnment", 0, 100, new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=(title:(*governnment*)) AND (content:(*governnment*))&start=0&rows=100&wt=json";
        endpoint = searchServiceHelper.build(siteSearchSolrQuery, "governnment", 0, 100, new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildCourseSearchString_CurrentPage_EqualstoZero() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery, "fraud", 0, 100, new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*Project Management*))^3 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*)) OR (course_abstract:(*Project Management*)) OR (course_prerequisites:(*Project Management*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";
        endpoint = searchServiceHelper.build(courseSearchSolrQuery, "Project Management", 0, 100, new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildSiteSearchString_CurrentPage_NegativeNumber() {
        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=(title:(*governnment*)) AND (content:(*governnment*))&start=0&rows=100&wt=json";
        String endpoint = searchServiceHelper.build(siteSearchSolrQuery, "governnment", -1, 100, new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/nutch-core/select?q=(title:(*governnment*)) AND (content:(*governnment*))&start=0&rows=100&wt=json";
        endpoint = searchServiceHelper.build(siteSearchSolrQuery, "governnment", -1, 100, new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildCourseSearchString_CurrentPage_NegativeNumber() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery, "fraud", -1, 100, new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*Project Management*))^3 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*)) OR (course_abstract:(*Project Management*)) OR (course_prerequisites:(*Project Management*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";
        endpoint = searchServiceHelper.build(courseSearchSolrQuery, "Project Management", -1, 100, new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildSearchStringWithCategorySubjectFacetParam() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=category_subject:\"Accounting, Budgetingand Financial Management/Financial Management\"&sort={sort}";
        String[] facetParam = {"category_subject:Accounting, Budgetingand Financial Management/Financial Management"};
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetParam);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&fq=category_subject:\"Accounting, Budgetingand Financial Management/Financial Management\"&sort={sort}";
        String[] facetParams = {"city_state:Washington","category_subject:Accounting, Budgetingand Financial Management/Financial Management"};
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetParams);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);

        final String TRIPLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&fq=status:\"S\"&fq=category_subject:\"Accounting, Budgetingand Financial Management/Financial Management\"&sort={sort}";
        String[] facetTripleParams = {"city_state:Washington","status:S","category_subject:Accounting, Budgetingand Financial Management/Financial Management"};
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetTripleParams);
        assertEquals(TRIPLE_TERM_RESULT, endpoint);
    }
    
    @Test 
    public void buildSearchWithNullArray() {
        final String NULL_ARRAY_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, null);
        assertEquals(NULL_ARRAY_RESULT, endpoint);
    }
    
    @Test 
    public void buildSearchWithArrayContainingNull() {
        final String NULL_ARRAY_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(*fraud*))^3 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*)) OR (course_abstract:(*fraud*)) OR (course_prerequisites:(*fraud*))&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&sort={sort}";
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, new String[] {null});
        assertEquals(NULL_ARRAY_RESULT, endpoint);
    }

}

