package com.everis.ct.web.step;

import com.everis.ct.web.service.aspect.evidence.ScreenShot;
import com.everis.ct.web.service.aspect.log.LogTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ScreenShot
@LogTime
public class GoogleSettingsStep {

    @Autowired
    private StepPages page;

    public void searchSettings(String option) {
        page.settinsPage().searchSettings(option);
    }

    public String getValueResult() {

        return page.settinsPage().getValueResult();
    }

}
