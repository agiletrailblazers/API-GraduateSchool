package com.gs.api.search.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TitleBuilder {

    @Value("${site.search.title.exclude}")
    private String siteSearchTitleExclude;

    
    /**
     * remove parts of the title we don't want to display
     * @param title the title
     * @param content the content I will use if the title is empty
     * @return title
     */
    public String parseTitle(String title, String content, String url) {
        if (StringUtils.isEmpty(title)) {
            if (StringUtils.endsWithAny(url, ".pdf", ".docx")) {
                title = StringUtils.substringAfterLast(url, "/");
            }
            else {
                title = StringUtils.substring(content,0,20);
            }
        }
        return StringUtils.replace(title, siteSearchTitleExclude, "").trim();
    }
    
}
