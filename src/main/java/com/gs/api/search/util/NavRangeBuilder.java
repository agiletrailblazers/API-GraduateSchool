package com.gs.api.search.util;

import org.springframework.stereotype.Service;

@Service
public class NavRangeBuilder {

    /**
     * Generate the page range to display for navigation. Show always display only 5 pages max attempting
     * to keep the current page in the "middle" of the range.
     * For example:
     *   - if current page is 3 of 10 show: 1,2,3,4,5
     *   - if current page is 7 of 10 show: 5,6,7,8,9
     *   - if current page is 10 of 10 show: 6,7,8,9,10
     * @param currentPage where am I currently
     * @param totalPages how many total pages are there
     * @return int[]
     */
    public int[] createNavRange(int currentPage, int totalPages) {
        int[] pageNavRange = new int[(totalPages > 5) ? 5 : totalPages];
        if (totalPages > 5) {
            if (currentPage - 2 <= 0) {
                //begin range
                for (int i=0; i<5; i++) {
                    pageNavRange[i] = i+1;
                }
            }
            else if (currentPage + 2 >= totalPages) {
                //end range
                int j = totalPages - 4;
                for (int i=0; i<5; i++) {
                    pageNavRange[i] = j;
                    j++;
                }
            }
            else {
                //mid range
                int j = currentPage - 2;
                for (int i=0; i<5; i++) {
                    pageNavRange[i] = j;
                    j++;
                }
            }
        }
        else {
            //range is less than 5
            for (int i=0; i<totalPages; i++) {
                pageNavRange[i] = i+1;
            }
        }
        return pageNavRange;
    }

}
