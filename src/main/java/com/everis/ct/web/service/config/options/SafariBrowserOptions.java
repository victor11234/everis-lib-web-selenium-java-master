package com.everis.ct.web.service.config.options;

import com.everis.ct.web.service.config.PropertiesVault;
import org.openqa.selenium.safari.SafariOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.logging.Level;

import static com.everis.ct.web.service.io.ManageFiles.readAsString;
import static com.everis.ct.web.service.util.UtilWeb.logger;

@Component
public class SafariBrowserOptions {

    @Autowired
    private PropertiesVault propertiesVault;

    private SafariOptions safariOptions;
    private boolean acceptInsecureCerts;

    @PostConstruct
    private void init() {
        safariOptions = new SafariOptions();
        acceptInsecureCerts = propertiesVault.isWebDriverAcceptInsecureCerts();
    }

    public SafariOptions fetchSafariOptions() {
        safariOptions.setCapability("acceptInsecureCerts", acceptInsecureCerts);
        logger(this.getClass()).log(Level.INFO, readAsString("logs/log-browser-options.txt"),
                new Object[]{"SafariOptions", safariOptions.asMap()});
        return safariOptions;
    }

}