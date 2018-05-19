package com.freebitcoin.app.control;

import com.twocaptcha.api.ProxyType;
import com.twocaptcha.api.TwoCaptchaService;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwoCaptchaFreeBTC {

    private static String responseToken;
    private static String apiKey;
    private static String googleKey;
    private static String pageUrl;
    private static TwoCaptchaService service;
    private static String proxyUser;
    private static String proxyPass;
    private static String proxy;
    private static String puerto;
    private Properties prop = new Properties();
    private final String PROP_PATH = "C:\\Users\\" + System.getProperty("user.name")
            + "\\AppData\\Roaming\\GT Tools\\config.properties";
    private static boolean sendProxy;

    public TwoCaptchaFreeBTC(String proxy, String puerto) throws FileNotFoundException {

        TwoCaptchaFreeBTC.googleKey = "6Lc6zQQTAAAAAD8TgxgC59CXtm1G56QLu8G7Q53K";
        TwoCaptchaFreeBTC.pageUrl = "https://freedoge.co.in/";
        TwoCaptchaFreeBTC.proxy = proxy.trim();
        TwoCaptchaFreeBTC.puerto = puerto.trim();
        try {

            prop.load(new FileReader(PROP_PATH));
            TwoCaptchaFreeBTC.apiKey = prop.getProperty("TwoCaptchaKey");
            TwoCaptchaFreeBTC.proxyUser = prop.getProperty("proxyUser");
            TwoCaptchaFreeBTC.proxyPass = prop.getProperty("proxyPass");
            TwoCaptchaFreeBTC.sendProxy = Boolean.valueOf(prop.getProperty("sendProxy"));

        } catch (IOException ex) {
            Logger.getLogger(TwoCaptchaFreeBTC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String Tokenizer() {

        if (sendProxy) {

            if (proxyUser.equals("Usuario") && proxyPass.equals("Contrasena")) {
                service = new TwoCaptchaService(apiKey, googleKey, pageUrl, proxy, puerto, ProxyType.HTTP);
                System.out.println("usando proxy sin auth");
            } else if (proxy.contains("0,0,0,0")) {
                System.out.println("sin proxy");
                service = new TwoCaptchaService(apiKey, googleKey, pageUrl);
            } else {
                System.out.println("usnado proxy con auth");
                service = new TwoCaptchaService(apiKey, googleKey, pageUrl, proxy, puerto, proxyUser, proxyPass, ProxyType.HTTP);
            }
        } else {
            System.out.println("com.freebitcoin.app.control.TwoCaptchaFreeBTC.Tokenizer()");
            System.out.println("sin proxy");
            service = new TwoCaptchaService(apiKey, googleKey, pageUrl);
        }
        
        try {
            responseToken = service.solveCaptcha();

            return responseToken;
        } catch (InterruptedException e) {
            System.out.println("ERROR case 1");
            e.printStackTrace();
            return responseToken;
        } catch (IOException e) {
            System.out.println("ERROR case 2");
            e.printStackTrace();
            return responseToken;
        } catch (Exception e) {
            return responseToken;
        }
    }
}
