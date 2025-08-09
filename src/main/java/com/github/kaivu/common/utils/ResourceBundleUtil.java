package com.github.kaivu.common.utils;

import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.exception.ObservableServiceException;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.config.handler.ErrorsEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@Slf4j
@ApplicationScoped
public class ResourceBundleUtil {

    private ResourceBundleUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getKeyWithResourceBundle(String bundleName, Locale locale, String key) {
        Locale localForBundle = locale != null ? locale : Locale.getDefault();
        return ResourceBundle.getBundle(bundleName, localForBundle).getString(key);
    }

    public static String getKeyWithResourceBundleOrThrow(String bundleName, Locale locale, String key) {
        try {
            return getKeyWithResourceBundle(bundleName, locale, key);
        } catch (Exception ex) {
            log.error("Resource bundle error: {}", ex.getMessage(), ex);
            try {
                ObservabilityContext context =
                        CDI.current().select(ObservabilityContext.class).get();
                throw new ObservableServiceException(
                        ErrorsEnum.SYSTEM_BUNDLE_DOES_NOT_EXIST.withLocale(locale), context, ex);
            } catch (Exception cdiEx) {
                log.warn(
                        "Could not get observability context, falling back to ServiceException: {}",
                        cdiEx.getMessage());
                throw new ServiceException(ErrorsEnum.SYSTEM_BUNDLE_DOES_NOT_EXIST.withLocale(locale));
            }
        }
    }

    public static Map<Locale, Map<String, String>> getAllMessages(String bundleName, List<Locale> locales) {
        Map<Locale, Map<String, String>> messages = new HashMap<>();

        locales.stream()
                .map(locate -> ResourceBundle.getBundle(bundleName, locate))
                .forEach(bundle -> {
                    Map<String, String> messagesChill = new HashMap<>();
                    Collections.list(bundle.getKeys()).forEach(key -> messagesChill.put(key, bundle.getString(key)));
                    messages.put(bundle.getLocale(), messagesChill);
                });
        return Collections.unmodifiableMap(messages);
    }
}
