package kz.zk.authservice.common.exception.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

/**
 * Enumeration for business error codes
 *
 */
public enum BusinessError {

    VALIDATION("Invalid request data. {0}", "Сұрау мәліметтері дұрыс емес. {0}", "Некорректные данные запроса. {0}", "Invalid request data. {0}"),
    ERROR("System error. Please try again later.", "Жүйелік қате. Кейінірек қайталап көріңіз.", "Системная ошибка. Повторите попытку позднее.", "System error. Please try again later."),
    TOO_MANY_TRIES("Too many attempts. Please try again later.", "Көптеген әрекеттер. Кейінірек қайталап көріңіз.", "Слишком много попыток. Пожалуйста, повторите позднее.", "Too many attempts. Please try again later."),
    DATABASE_ERROR("Database error.", "Деректер базасындағы қате.", "Ошибка базы данных.", "Database error.");

    @Getter
    private final String defaultMessage;
    @Getter
    private final String messageKK;
    @Getter
    private final String messageRU;
    @Getter
    private final String messageEN;

    BusinessError(String defaultMessage, String messageKK, String messageRU, String messageEN) {
        this.defaultMessage = defaultMessage;
        this.messageKK = messageKK;
        this.messageRU = messageRU;
        this.messageEN = messageEN;
    }

    public String getLocalizedMessage() {
        Locale locale = LocaleContextHolder.getLocale();
        if(Objects.nonNull(locale) && StringUtils.equalsIgnoreCase("kk", locale.getLanguage())) {
            return messageKK;
        }
        else if(Objects.nonNull(locale) && StringUtils.equalsIgnoreCase("ru", locale.getLanguage())) {
            return messageRU;
        }
        else if(Objects.nonNull(locale) && StringUtils.equalsIgnoreCase("en", locale.getLanguage())) {
            return messageEN;
        }
        return defaultMessage;
    }
}
