package kz.zk.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
@Service
public class UtilService {

    /**
     * Get localized message
     *
     * @param key message key
     * @param args message arguments
     * @return localized message
     */
    public static String getLocalizedMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            log.warn("Message not found for key '{}', locale '{}'", key, locale);
            return "???" + key + "???";
        }
    }

}
