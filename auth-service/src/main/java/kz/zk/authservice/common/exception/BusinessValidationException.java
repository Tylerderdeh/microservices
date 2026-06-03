package kz.zk.authservice.common.exception;


import kz.zk.authservice.common.exception.constant.BusinessError;
import kz.zk.authservice.service.UtilService;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class BusinessValidationException extends BusinessException {

    private static final BusinessError error = BusinessError.VALIDATION;
    protected final String key;

    public String getKey() {
        return key;
    }

    public BusinessValidationException(String key) {
        super(error.name(), MessageFormat.format(error.getLocalizedMessage(),  UtilService.getLocalizedMessage(key)));
        this.key = key;
    }

    public BusinessValidationException(String key, Object... args) {
        super(error.name(), MessageFormat.format(error.getLocalizedMessage(),  UtilService.getLocalizedMessage(key, args)));
        this.key = key;
    }

    public static Supplier<BusinessValidationException> exceptionSupplier(String key) {
        return () -> new BusinessValidationException(key);
    }

    public static Supplier<BusinessValidationException> exceptionSupplier(String key, Object... args) {
        return () -> new BusinessValidationException(key, args);
    }

}
