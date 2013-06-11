/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.frontend.web.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.orcid.core.manager.StatisticsManager;
import org.orcid.persistence.jpa.entities.StatisticValuesEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StatisticsController extends BaseController {

    @Resource
    private StatisticsManager statisticsManager;
    
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public ModelAndView getStatistics() {        
        ModelAndView mav = new ModelAndView("statistics");
        Map<String, Long> statisticsMap = new HashMap<String, Long>();
                
        List<StatisticValuesEntity> statistics = statisticsManager.getLatestStatistics();
        
        for(StatisticValuesEntity statistic : statistics){
            System.out.println(statistic.getStatisticName() + " - " + statistic.getStatisticValue());
            statisticsMap.put(statistic.getStatisticName(), statistic.getStatisticValue());
        }        
        
        mav.addObject("statistics", statisticsMap);        
       
        return mav;
    }    
}
