package com.github.kaivu.common.context;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

import java.util.Locale;

/**
 * Request-scoped bean to access current language context
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 7/27/25
 * Time: 2:30 AM
 */
@RequestScoped
public class LanguageContext {

    @Context
    private ContainerRequestContext requestContext;

    /**
     * Get the current request language
     * @return current language as string, defaults to "en" if not specified
     */
    public String getCurrentLanguage() {
        if (requestContext != null && requestContext.getLanguage() != null) {
            return requestContext.getLanguage().toString();
        }
        return "en"; // default language
    }

    /**
     * Get the current request language as Locale
     * @return current language as Locale, defaults to English if not specified
     */
    public Locale getCurrentLocale() {
        if (requestContext != null && requestContext.getLanguage() != null) {
            return requestContext.getLanguage();
        }
        return Locale.ENGLISH; // default locale
    }

    /**
     * Check if a specific language is currently active
     * @param language language code to check
     * @return true if the current language matches
     */
    public boolean isCurrentLanguage(String language) {
        return getCurrentLanguage().equalsIgnoreCase(language);
    }
}
