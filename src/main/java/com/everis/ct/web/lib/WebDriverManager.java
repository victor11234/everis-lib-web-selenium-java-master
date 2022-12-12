package com.everis.ct.web.lib;

import com.everis.ct.web.service.config.CheckProperties;
import com.everis.ct.web.service.constans.Browser;
import com.everis.ct.web.service.io.ManageFiles;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;

import static com.everis.ct.web.service.util.UtilWeb.logger;

@Configuration
public class WebDriverManager extends SetDriver implements IWebDriverManager {

    private static final ThreadLocal<WebDriver> threadLocal = new ThreadLocal<>();

    public void setDriver(WebDriver driver) {
        threadLocal.set(driver);
    }

    public void removeThread() {
        threadLocal.remove();
    }

    /**
     * Obtiene el Driver que se esta ejecutando en ese momento copiado por la clase ThreadLocal
     *
     * @return retonra el Driver ejecutandose en un hilo especifico administrado por la clase ThreadLocal.
     */
    public static WebDriver getDriver() {
        return threadLocal.get();
    }

    /**
     * Metodo que crea la intancia del nuevo Driver a partir de un browser soportado.
     * Los browser soportados para este metodo son: chrome | safari | firefox | edge
     */
    @Override
    public void setUpDriver() {
        var browser = propertiesVault.getBrowser();
        var implicitWait = propertiesVault.getImplicitWait();
        var windowsSize = propertiesVault.getWebDriverSize();

        logger(this.getClass()).log(Level.INFO, "BrowserName >>> {0}.", browser);
        logger(this.getClass()).log(Level.INFO, "ImplicitWait >>> {0} seconds.", implicitWait);
        if (CheckProperties.isDefinided(browser) && !browser.isEmpty()) {
            WebDriver driver;
            switch (browser.toUpperCase(Locale.ROOT)) {
                case Browser.CHROME:
                    driver = configChromeDriver();
                    break;
                case Browser.FIREFOX:
                    driver = configFirefoxDriver();
                    break;
                case Browser.SAFARI:
                    driver = configSafariDriver();
                    break;
                case Browser.EDGE:
                    driver = configEdgeDriver();
                    break;
                default:
                    logger(this.getClass()).log(Level.WARNING,
                            ManageFiles.readAsString("logs/log-not-supported-browser.txt"), browser);
                    throw new IllegalArgumentException();
            }
            setDriver(driver);
            configWindowsSize(windowsSize);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        } else {
            logger(this.getClass()).warning("Property \"webdriver.browser\" is not defined in file \"application.properties\".");
            throw new IllegalArgumentException();
        }
    }

    private void configWindowsSize(String windowsSize) {
        switch (windowsSize.toUpperCase(Locale.ROOT)) {
            case "":
            case Browser.WIN_SIZE_MAXIMIZE:
                maximize();
                break;
            case Browser.WIN_SIZE_FULLSCREEN:
                fullScreen();
                break;
            case Browser.WIN_SIZE_NONE:
            default:
                logger(this.getClass()).log(Level.INFO, ManageFiles.readAsString("logs/log-browser-windows-size.txt"),
                        windowsSize);
                break;
        }
    }

    @Override
    public void navigateTo(String url) {
        logger(this.getClass()).log(Level.INFO, "Navigating to website >>> \"{0}\", on Thread - \"{1}, {2}\"",
                new Object[]{url, Thread.currentThread().getName(), Thread.currentThread().getId()});
        getDriver().navigate().to(url);
    }

    /**
     * Maximiza la dimension de la venta del browser.
     */
    @Override
    public void maximize() {
        logger(this.getClass()).info("Maximizing website.");
        getDriver().manage().window().maximize();
    }

    /**
     * Amplia las dimensiones de la ventana a Pantalla completa del browser.
     */
    @Override
    public void fullScreen() {
        logger(this.getClass()).info("FullScreen website.");
        getDriver().manage().window().fullscreen();
    }

    /**
     * Detiene el Driver del browser ejecutado.
     */
    @Override
    public void quitDriver() {
        if (isDriverOn())
            getDriver().quit();
        else
            logger(this.getClass()).warning("Driver session does not exist.");
    }

    /**
     * Valida si actualmente existe algun Driver ejectuandose
     *
     * @return retorna verdadero o falso dependiendo del estado del Driver.
     */
    @Override
    public boolean isDriverOn() {
        return getDriver() != null;
    }

}