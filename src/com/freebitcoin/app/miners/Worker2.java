package com.freebitcoin.app.miners;

import com.freebitcoin.app.control.Proxies;
import com.freebitcoin.app.control.TwoCaptchaFreeBTC;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Worker2 extends SwingWorker<Boolean, String> {

    private static String balanceDoge = "";
    private static String rollDoge = "";
    private static WebDriver driver;
    private final String perfil;
    private final int selector;
    private final DefaultTableModel model;
    private final File file;
    private final boolean BackGroundStatus;
    private static String responseToken;
    private LocalDateTime now;
    private final LocalDateTime[] nextRollArray;
    private final ArrayList<Double> balanceRollArray;
    private final Double[] balanceTotalArray;
    private final ArrayList<Proxies> proxies;
    private int captchaCount = 1,
            proxySelector;
    private int[] procesando;

    public Worker2(int selector, DefaultTableModel model,
            boolean backGroundStatus, LocalDateTime[] nextRollArray,
            ArrayList<Double> balanceRollArray, Double[] balanceTotalArray,
            ArrayList<Proxies> proxies, int[] procesando) {

        this.perfil = (String) model.getValueAt(selector, 1);
        this.selector = selector;
        this.proxySelector = selector;
        this.model = model;
        this.BackGroundStatus = backGroundStatus;
        this.nextRollArray = nextRollArray;
        this.balanceRollArray = balanceRollArray;
        this.balanceTotalArray = balanceTotalArray;
        this.proxies = proxies;
        this.procesando = procesando;
        this.file = new File("C:\\Program Files\\GT Tools\\geckodriver.exe");
        procesando[1] = 1;
    }

    @Override
    protected Boolean doInBackground() throws Exception {

        try {

            Inicializar(perfil);
            loadSite();
            rollAction();
            //freeRollPlay();
            postear();
            procesando[1] = 0;
            return false;
        } catch (NoSuchSessionException ex) {
            procesando[1] = 0;
            return false;
        } catch (WebDriverException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            model.setValueAt("  Ha ocurrido un error", selector, 5);
            now = LocalDateTime.now().plusMinutes(2);
            nextRollArray[selector] = now;
            procesando[1] = 0;
            driver.quit();

            return false;
        } catch (Exception e) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, e);
            model.setValueAt("  Ha ocurrido un error", selector, 5);
            now = LocalDateTime.now().plusMinutes(2);
            nextRollArray[selector] = now;
            procesando[1] = 0;
            driver.quit();
            return false;
        }
    }

    @Override
    protected void done() {
    }

    protected void Inicializar(String Perfil) throws WebDriverException, InterruptedException {
        model.setValueAt("  Cargando perfil... ", selector, 5);
        System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
        System.setProperty("webdriver.firefox.bin", "C:\\Program Files\\Mozilla Firefox\\firefox.exe");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile myprofile = profile.getProfile(Perfil);

        FirefoxOptions options = new FirefoxOptions().setProfile(myprofile);

        options.addPreference("signon.autologin.proxy", true);
        if (BackGroundStatus) {
            options.addArguments("--headless");
        }
        model.setValueAt("  Abriendo navegador...", selector, 5);
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        model.setValueAt("  Cargando https://freedoge.co.in/", selector, 5);
        try {
            driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
            driver.get("https://freedoge.co.in/");
        } catch (TimeoutException ex) {
            // Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            driver.quit();
            Inicializar(perfil);
        }
    }

    protected void loadSite() throws InterruptedException, NoSuchSessionException, WebDriverException {
        try {
            balanceDoge = driver.findElement(By.id("balance")).getText();
            System.out.println(balanceDoge);
        } catch (NoSuchElementException e) {
            driver.quit();
            model.setValueAt(" Sesión no iniciada", selector, 14);
            throw new NoSuchSessionException("No inicio");
        }
        driver.findElement(By.linkText("FREE DOGE")).click();

        if (driver.findElement(By.id("time_remaining")).isDisplayed()) {

            String timeRemaining = driver.findElement(By.id("time_remaining")).getText();
            String hora = "";
            try {
                int minutos = Integer.parseInt(timeRemaining.substring(0, 2).trim());
                hora = LocalTime.now().plus(minutos + 2, ChronoUnit.MINUTES).format(DateTimeFormatter.ofPattern("hh:mm a"));
                now = LocalDateTime.now().plus(minutos, ChronoUnit.MINUTES);
            } catch (NumberFormatException e) {
                hora = LocalTime.now().plus(5, ChronoUnit.MINUTES).format(DateTimeFormatter.ofPattern("hh:mm a"));
                now = LocalDateTime.now().plus(5, ChronoUnit.MINUTES);
            }
            nextRollArray[selector] = now;

            double balanceParse = Double.parseDouble(balanceDoge);
            model.setValueAt(balanceParse, selector, 2);

            balanceRollArray.add(0.0);
            model.setValueAt(hora, selector, 4);
            model.setValueAt("  Esperando siguiente ronda", selector, 5);
            balanceTotalArray[selector] = balanceParse;
            driver.quit();
            driver.findElement(By.linkText("FREE DOGE")).click();
        }
    }

     protected void rollAction() throws IOException, InterruptedException, NoSuchSessionException {
        model.setValueAt("  Resolviendo Captcha... Intento " + captchaCount, selector, 5);

        if (captchaCount == 4) {
            model.setValueAt("CAPTCHA_TIMEOUT", selector, 5);
            now = LocalDateTime.now().plusMinutes(1);
            nextRollArray[selector] = now;
            driver.quit();
        }

        driver.findElement(By.linkText("FREE DOGE")).click();

        TwoCaptchaFreeBTC prueba = new TwoCaptchaFreeBTC(proxies.get(proxySelector).getProxy(), proxies.get(proxySelector).getPuerto());
        responseToken = prueba.Tokenizer();

        if (responseToken.contains("ERROR_WRONG_USER_KEY")) {
            model.setValueAt("  2Captcha API Key invalido", selector, 14);

        } else if (responseToken.contains("ERROR_RECAPTCHA_TIMEOUT") & captchaCount >= 1) {

            do {
                int range = (model.getRowCount() - 1);
                proxySelector = (int) (Math.random() * range + 0);
                System.out.println("Muchos intentos usando proxy en: " + proxySelector);
            } while (proxySelector == selector);
            captchaCount++;
            rollAction();

        } else if (responseToken.contains("ERROR_PROXY_BANNED")) {
            do {
                int range = (model.getRowCount() - 1);
                proxySelector = (int) (Math.random() * range + 0);
                System.out.println("Usando proxy en: " + proxySelector);
            } while (proxySelector == selector);
            captchaCount++;
            rollAction();
        }

        System.out.println(responseToken);
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("document.getElementById('g-recaptcha-response').value='" + responseToken + "';");
        try {
            driver.findElement(By.linkText("Got it!")).click();
        } catch (NoSuchElementException e) {

            System.out.println("El banner no esta activo");
        }
        try {
            driver.findElement(By.id("free_play_form_button")).click(); // Roll
        } catch (ElementNotInteractableException e) {
            System.out.println("EL boton de roll no esta interactuable");
        }
        
        Thread.sleep(2000);
        if (driver.findElement(By.id("free_play_error")).isDisplayed()) {
            if (driver.findElement(By.id("free_play_error")).getText().
                    equals("Sorry, this IP address has been blocked. If"
                            + " you are using a proxy, VPN or anonymization "
                            + "service, please turn it off before claiming "
                            + "free bitcoins.")) {
                ipBaned();
            } else if (driver.findElement(By.id("free_play_error")).getText().
                    equals("Captcha is incorrect or has expired. Please try again.")) {
                System.out.print("Captcha incorrecto");
                captchaCount++;
                rollAction();
            } else if (driver.findElement(By.id("free_play_error")).getText().contains("You "
                    + "need to verify your email before you can play the FREE BTC game.")) {
                model.setValueAt(" Necesita verificar email", selector, 14);
                nextRollArray[selector] = LocalDateTime.now().plusDays(2);
                driver.quit();
            }
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 5);
            WebElement element = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("winnings")));
        } catch (Exception e) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, e);
            driver.navigate().refresh();
            rollAction();
        }
        rollDoge = driver.findElement(By.id("winnings")).getText();
        balanceDoge = driver.findElement(By.id("balance")).getText();
        model.setValueAt("  ¡Roll listo!", selector, 5);
        model.setValueAt("  Esperando siguiente ronda", selector, 5);
    }
     
    protected void postear() throws InterruptedException, NoSuchSessionException {

        now = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
        String hora = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
        double balroll = 0.0;
        System.out.println(balanceDoge);
        double balanceParse = Double.parseDouble(balanceDoge);
        System.out.println(balanceParse);
        if (!rollDoge.isEmpty()) {
            balroll = Double.parseDouble(rollDoge);
            model.setValueAt(balroll, selector, 3);
            model.setValueAt(balanceParse, selector, 2);

        } else {
            model.setValueAt(0, selector, 3);
        }

        model.setValueAt(hora, selector, 4);

        balanceRollArray.add(balroll);
        nextRollArray[selector] = now;
        balanceTotalArray[selector] = balanceParse;
        driver.quit();
    }

    protected void ipBaned() throws NoSuchSessionException {

        double balanceParse;
        if (!balanceDoge.isEmpty()) {
            balanceParse = Double.parseDouble(balanceDoge);
            balanceTotalArray[selector] = balanceParse;
            model.setValueAt(balanceParse, selector, 2);
        } else {
            balanceParse = 0;
            balanceTotalArray[selector] = balanceParse;
            model.setValueAt(0, selector, 2);
        }
        now = LocalDateTime.now().plus(2, ChronoUnit.DAYS);
        balanceRollArray.add(0.0);
        model.setValueAt(balanceParse, selector, 2);
        model.setValueAt(0, selector, 3);
        model.setValueAt("-", selector, 4);
        model.setValueAt("  IP Baneada", selector, 5);
        nextRollArray[selector] = now;
        driver.quit();
        driver.findElement(By.id("free_play_form_button")).click();
    }

    protected void freeRollPlay() throws NoSuchSessionException, InterruptedException, IOException {
        driver.findElement(By.linkText("FREE DOGE")).click();
        model.setValueAt("Intentando Roll gratis", selector, 5);
        try {
            driver.findElement(By.className("g-recaptcha"));
            rollAction();
            //  Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchElementException e) {
            // Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, e);
            sinCaptpcha();
        }
    }

    private void sinCaptpcha() throws NoSuchSessionException, InterruptedException, IOException {
        try {
            driver.findElement(By.linkText("Got it!")).click();
        } catch (Exception e) {

            System.out.println("El banner no esta activo");
        }

        driver.findElement(By.id("free_play_form_button")).click();
        try {
            WebDriverWait wait = new WebDriverWait(driver, 5);
            WebElement element = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("free_play_result")));
        } catch (Exception e) {
            // Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, e);
            driver.navigate().refresh();
            rollAction();
        }
        model.setValueAt(" ¡Roll listo!", selector, 5);
        model.setValueAt(" Esperando siguiente ronda.", selector, 5);
        postear();
    }

}
