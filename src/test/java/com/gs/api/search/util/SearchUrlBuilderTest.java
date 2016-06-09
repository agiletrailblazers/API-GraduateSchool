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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestOperations;

import com.gs.api.search.util.SearchUrlBuilder;

import java.util.ArrayList;

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

        String[] blacklistConnectorsArray = {"of","and","in","the","to","a","for","by","with"};
        ReflectionTestUtils.setField(searchServiceHelper, "courseSearchSolrBlacklistConnectors", blacklistConnectorsArray);

        String[] blacklistTermsArray = {"introduction","basic","intermediate","advanced"};
        ReflectionTestUtils.setField(searchServiceHelper, "courseSearchSolrBlacklistTerms", blacklistTermsArray);
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
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100,new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"Project Management\"))^100 OR (course_name:(*Project Management*))^9 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*))^0.00001 OR (course_abstract:(*Project Management*))^0.00001 OR (course_prerequisites:(*Project Management*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"Project Management",1, 100,new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildSearchStringWithFacetParam() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        String[] facetParam = {"city_state:Washington"};
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetParam);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"Project Management\"))^100 OR (course_name:(*Project Management*))^9 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*))^0.00001 OR (course_abstract:(*Project Management*))^0.00001 OR (course_prerequisites:(*Project Management*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&fq=status:\"S\"&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
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
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery, "fraud", 0, 100, new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"Project Management\"))^100 OR (course_name:(*Project Management*))^9 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*))^0.00001 OR (course_abstract:(*Project Management*))^0.00001 OR (course_prerequisites:(*Project Management*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
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
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery, "fraud", -1, 100, new String[0]);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        //two terms
        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"Project Management\"))^100 OR (course_name:(*Project Management*))^9 OR (course_id:(*Project Management*))^9 OR (course_code:(*Project Management*))^6 OR (course_description:(*Project Management*))^0.00001 OR (course_abstract:(*Project Management*))^0.00001 OR (course_prerequisites:(*Project Management*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        endpoint = searchServiceHelper.build(courseSearchSolrQuery, "Project Management", -1, 100, new String[0]);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);
    }

    @Test
    public void buildSearchStringWithCategorySubjectFacetParam() {

        //single term
        final String SINGLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=category_subject:\"Accounting, Budgetingand Financial Management/Financial Management\"&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        String[] facetParam = {"category_subject:Accounting, Budgetingand Financial Management/Financial Management"};
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetParam);
        assertEquals(SINGLE_TERM_RESULT, endpoint);

        final String DOUBLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&fq=category_subject:\"Accounting, Budgetingand Financial Management/Financial Management\"&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        String[] facetParams = {"city_state:Washington","category_subject:Accounting, Budgetingand Financial Management/Financial Management"};
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetParams);
        assertEquals(DOUBLE_TERM_RESULT, endpoint);

        final String TRIPLE_TERM_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&fq=city_state:\"Washington\"&fq=status:\"S\"&fq=category_subject:\"Accounting, Budgetingand Financial Management/Financial Management\"&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        String[] facetTripleParams = {"city_state:Washington","status:S","category_subject:Accounting, Budgetingand Financial Management/Financial Management"};
        endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, facetTripleParams);
        assertEquals(TRIPLE_TERM_RESULT, endpoint);
    }
    
    @Test 
    public void buildSearchWithNullArray() {
        final String NULL_ARRAY_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, null);
        assertEquals(NULL_ARRAY_RESULT, endpoint);
    }
    
    @Test 
    public void buildSearchWithArrayContainingNull() {
        final String NULL_ARRAY_RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"fraud\"))^100 OR (course_name:(*fraud*))^9 OR (course_id:(*fraud*))^9 OR (course_code:(*fraud*))^6 OR (course_description:(*fraud*))^0.00001 OR (course_abstract:(*fraud*))^0.00001 OR (course_prerequisites:(*fraud*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";
        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"fraud", 1, 100, new String[] {null});
        assertEquals(NULL_ARRAY_RESULT, endpoint);
    }

    @Test
    public void buildSearchWithArrayContainingConnectorWords() {
        final String RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"a search of and that in contains the to for by with all connector words\"))^100 OR (course_name:(*search that contains all connector words*))^9 OR (course_id:(*search that contains all connector words*))^9 OR (course_code:(*search that contains all connector words*))^6 OR (course_description:(*search that contains all connector words*))^0.00001 OR (course_abstract:(*search that contains all connector words*))^0.00001 OR (course_prerequisites:(*search that contains all connector words*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"a search of and that in contains the to for by with all connector words", 1, 100,new String[0]);
        assertEquals(RESULT, endpoint);
    }

    @Test
    public void buildSearchWithArrayContainingBlacklistedTerms() {
        final String RESULT = "http://ec2-52-2-60-235.compute-1.amazonaws.com:9090/solr/courses/select?q=(course_name:(\"Advanced Introduction to Finance with Basic and Intermediate\"))^100 OR (course_name:(*Finance*))^9 OR (course_id:(*Finance*))^9 OR (course_code:(*Finance*))^6 OR (course_description:(*Finance*))^0.00001 OR (course_abstract:(*Finance*))^0.00001 OR (course_prerequisites:(*Finance*))^0.00001&fq=course_description:[* TO *]&start=0&rows=100&wt=json&indent=true&group=true&group.field=course_id&group.facet=true&group.ngroups=true&group.format=simple&facet=true&facet.field={facet-exclude}city_state&facet.field=status&facet.field={facet-exclude}category_subject&sort={sort}&f.category_subject.facet.sort=index&facet.field={facet-exclude}delivery_method&f.delivery_method.facet.sort=index";

        String endpoint = searchServiceHelper.build(courseSearchSolrQuery,"Advanced Introduction to Finance with Basic and Intermediate", 1, 100,new String[0]);
        assertEquals(RESULT, endpoint);
    }

}

