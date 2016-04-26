package com.gs.api.service;

import com.gs.api.dao.CommonDAO;
import com.gs.api.domain.registration.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommonServiceImpl implements CommonService {

    final Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);

    @Autowired
    private CommonDAO commonDAO;


    @Override
    public List<Timezone> getTimezones() throws Exception {
        logger.debug("Getting the list of timezones");

        return commonDAO.getTimezones();
    }
}
