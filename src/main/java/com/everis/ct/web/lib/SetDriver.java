package com.everis.ct.web.lib;

import com.everis.ct.web.service.config.CheckProperties;
import com.everis.ct.web.service.config.PropertiesVault;
import com.everis.ct.web.service.config.options.BaseOptions;
import com.everis.ct.web.service.constans.Browser;
import com.everis.ct.web.service.util.UtilWeb;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;

import static com.everis.ct.web.service.constans.DriverProperties.*;
import static com.everis.ct.web.service.util.UtilWeb.logger;

@Configuration
public class SetDriver {

    @Autowired
    protected PropertiesVault propertiesVault;
    @Autowired
    private BaseOptions options;

    private boolean isRemote;
    private boolean isCICDExecution;
    private String remoteHub;
    private String driverPath;
    private static final String DRIVER_LOCATED_ENVIRONMENT_PATH = "Driver located on Environment variable PATH";

    @PostConstruct
    public void initProperties() {
        isCICDExecution = propertiesVault.isCicdExecution();
        isRemote = propertiesVault.isWebDriverRemote();
        remoteHub = propertiesVault.getWebDriverHub();
        driverPath = propertiesVault.getDriverPath();
        logger(WebDriverManager.class).log(Level.INFO, "CI/CD Execution >>> {0}.", isCICDExecution);
        logger(WebDriverManager.class).log(Level.INFO, "RemoteExecution >>> {0}.", isRemote);
        if (isRemote)
            logger(WebDriverManager.class).log(Level.INFO, "Using remote nodes configured in hub {0}.", remoteHub);
        else
            logger(WebDriverManager.class).log(Level.INFO, "DriverPath >>> \"{0}\".", driverPath);
    }

    protected WebDriver configChromeDriver() {
        WebDriver driver;
        if (isRemote) {
            driver = setUpRemoteHub(options.getChrome().fetchChromeOptions(), remoteHub);
        } else {
            if (!isCICDExecution) setWebDriverPropertyTo(Browser.CHROME);
            driver = new ChromeDriver(options.getChrome().fetchChromeOptions());
        }
        return driver;
    }

    protected WebDriver configFirefoxDriver() {
        WebDriver driver;
        if (isRemote) {
            driver = setUpRemoteHub(options.getFirefox().fetchFirefoxOptions(), remoteHub);
        } else {
            if (!isCICDExecution) setWebDriverPropertyTo(Browser.FIREFOX);
            driver = new FirefoxDriver(options.getFirefox().fetchFirefoxOptions());
        }
        return driver;
    }

    protected WebDriver configSafariDriver() {
        WebDriver driver;
        if (isRemote)
            driver = setUpRemoteHub(options.getSafari().fetchSafariOptions(), remoteHub);
        else
            driver = new SafariDriver(options.getSafari().fetchSafariOptions());
        return driver;
    }

    protected WebDriver configEdgeDriver() {
        WebDriver driver;
        if (isRemote) {
            driver = setUpRemoteHub(options.getEdge().fetchEdgeOptions(), remoteHub);
        } else {
            if (!isCICDExecution) setWebDriverPropertyTo(Browser.EDGE);
            driver = new EdgeDriver(options.getEdge().fetchEdgeOptions());
        }
        return driver;
    }

    private WebDriver setUpRemoteHub(MutableCapabilities capabilities, String driverHub) {
        final WebDriver driver;
        try {
            driver = new RemoteWebDriver(new URL(driverHub), capabilities);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(propertiesVault.getImplicitWait()));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Error in \"setUpRemoteHub();\" >>>" + e.getMessage());
        }
        return driver;
    }

    private void setWebDriverPropertyTo(String browser) {
        switch (browser.toUpperCase(Locale.ROOT)) {
            case Browser.CHROME:
                chromeDriverPropertyPath();
                break;
            case Browser.FIREFOX:
                firefoxDriverropertyPath();
                break;
            case Browser.EDGE:
                edgeDriverropertyPath();
                break;
            default:
                throw new IllegalArgumentException("Browser name not supported '" + browser + "'");
        }
    }

    private void chromeDriverPropertyPath() {
        var driverPathChrome = propertiesVault.getDriverPathChrome();
        if (CheckProperties.isDefinided(driverPath) && !driverPath.isEmpty())
            System.setProperty(CHROME_PROPERTY, driverPath);
        else if (CheckProperties.isDefinided(driverPathChrome) && !driverPathChrome.isEmpty())
            System.setProperty(CHROME_PROPERTY, driverPathChrome);
        else
            UtilWeb.logger(this.getClass()).info(DRIVER_LOCATED_ENVIRONMENT_PATH);
    }

    private void firefoxDriverropertyPath() {
        var driverPathFirefox = propertiesVault.getDriverPathFirefox();
        if (CheckProperties.isDefinided(driverPath) && !driverPath.isEmpty())
            System.setProperty(FIREFOX_PROPERTY, driverPath);
        else if (CheckProperties.isDefinided(driverPathFirefox) && !driverPathFirefox.isEmpty())
            System.setProperty(FIREFOX_PROPERTY, driverPathFirefox);
        else
            UtilWeb.logger(this.getClass()).info(DRIVER_LOCATED_ENVIRONMENT_PATH);
        //Experimental
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
    }

    private void edgeDriverropertyPath() {
        var driverPathEdge = propertiesVault.getDriverPathEdge();
        if (CheckProperties.isDefinided(driverPath) && !driverPath.isEmpty())
            System.setProperty(EDGE_PROPERTY, propertiesVault.getDriverPath());
        else if (CheckProperties.isDefinided(driverPathEdge) && !driverPathEdge.isEmpty())
            System.setProperty(EDGE_PROPERTY, driverPathEdge);
        else
            UtilWeb.logger(this.getClass()).info(DRIVER_LOCATED_ENVIRONMENT_PATH);
    }

}