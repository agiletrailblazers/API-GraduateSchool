package com.gs.api.service;

import com.gs.api.domain.registration.Timezone;

import java.util.List;

public interface CommonService {

    /**
     * Gets a list of timezones
     * @return the list of timezones
     * @throws Exception error getting timezones
     */
    List<Timezone> getTimezones() throws Exception;
}
