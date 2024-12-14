package vn.fpt.config;

import io.quarkus.qute.Qute;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.MessageInterpolator;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.extern.slf4j.Slf4j;
import vn.fpt.constant.AppConstant;
import vn.fpt.utils.ResourceBundleUtil;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class AppMessageInterpolator implements MessageInterpolator {

    private static final Set<String> ALLOWED_ATTRIBUTES = Set.of("max", "min", "regexp", "value");
    private static final Map<String, String> MESSAGE_CACHE = new ConcurrentHashMap<>();

    @jakarta.ws.rs.core.Context
    ContainerRequestContext requestContext;

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, requestContext.getLanguage());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return MESSAGE_CACHE.computeIfAbsent(
                messageTemplate + locale.toString(),
                key -> processMessage(messageTemplate, context, locale)
        );
    }

    private String processMessage(String messageTemplate, Context context, Locale locale) {
        try {
            String messageBundle = ResourceBundleUtil.getKeyWithResourceBundle(
                    AppConstant.I18N_VALIDATION,
                    locale,
                    messageTemplate
            );

            Map<String, Object> filteredAttributes = context.getConstraintDescriptor()
                    .getAttributes()
                    .entrySet()
                    .stream()
                    .filter(entry -> ALLOWED_ATTRIBUTES.contains(entry.getKey()))
                    .collect(Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));

            return Qute.fmt(messageBundle, filteredAttributes);
        } catch (Exception ex) {
            log.warn("Message interpolation failed for template '{}': {}", messageTemplate, ex.getMessage());
            return messageTemplate;
        }
    }
}
