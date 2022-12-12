package com.everis.ct.web.service.config.options;

import com.everis.ct.web.lib.WebDriverManager;
import com.everis.ct.web.service.config.PropertiesVault;
import com.everis.ct.web.service.constans.Browser;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static com.everis.ct.web.service.config.CheckProperties.isDefinided;
import static com.everis.ct.web.service.io.ManageFiles.readAsString;
import static com.everis.ct.web.service.util.UtilWeb.logger;

@Component
public class EdgeBrowserOptions {

    @Autowired
    private PropertiesVault propertiesVault;

    private boolean edgeExtension;
    private String edgeExtensionName;
    private String edgeExtensionPathCRX;
    private List<String> edgeExtraArgs;
    private String pageLoad;
    private EdgeOptions edgeOptions;
    private boolean acceptInsecureCerts;

    @PostConstruct
    private void init() {
        edgeOptions = new EdgeOptions();
        acceptInsecureCerts = propertiesVault.isWebDriverAcceptInsecureCerts();
        edgeExtraArgs = propertiesVault.getEdgeExtraArgsOptions();
        pageLoad = propertiesVault.getPageLoadStrategy();

        //Extensions
        edgeExtension = propertiesVault.isWebDriverExtensionsOn();
        edgeExtensionName = propertiesVault.getWebDriverExtensionName();
        edgeExtensionPathCRX = propertiesVault.getWebDriverExtensionPathCRX();
    }

    public EdgeOptions fetchEdgeOptions() {
        edgeOptions.setAcceptInsecureCerts(acceptInsecureCerts);
        if (edgeExtension) {
            logger(WebDriverManager.class).log(Level.INFO, "Adding extension >>> {0}", edgeExtensionName);
            edgeOptions.addExtensions(new File(edgeExtensionPathCRX));
        }
        setExtrasArgumentsOptions(edgeOptions);
        setPageLoadStrategy();
        if (edgeExtension)
            logger(this.getClass()).log(Level.INFO, readAsString("logs/log-browser-options.txt"),
                    new Object[]{"EdgeOptions", edgeOptions.getCapabilityNames()});
        else
            logger(this.getClass()).log(Level.INFO, readAsString("logs/log-browser-options.txt"),
                    new Object[]{"EdgeOptions", edgeOptions.asMap()});
        return edgeOptions;
    }

    private void setPageLoadStrategy() {
        if (pageLoad.toUpperCase(Locale.ROOT).equals(Browser.PAGE_LOAD_STRATEGY_EAGER)) {
            edgeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
        } else if (pageLoad.toUpperCase(Locale.ROOT).equals(Browser.PAGE_LOAD_STRATEGY_NONE)) {
            edgeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
        } else {
            logger(WebDriverManager.class).log(Level.INFO, "Normal page loader strategy.");
        }
    }

    private void setExtrasArgumentsOptions(EdgeOptions edgeOptions) {
        if (isDefinided(edgeExtraArgs)) {
            logger(WebDriverManager.class).log(Level.INFO, "EdgeOptions extra arguments >>> True.");
            logger(WebDriverManager.class).log(Level.INFO, "(+) Otros argumentos {0}", edgeExtraArgs);
            for (String args : edgeExtraArgs) {
                logger(WebDriverManager.class).log(Level.INFO, "(+) Argument-EdgeOption {0}", args);
                edgeOptions.addArguments(args.trim());
            }
        }
    }

}