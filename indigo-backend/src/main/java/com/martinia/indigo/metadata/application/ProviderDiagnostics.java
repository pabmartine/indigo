package com.martinia.indigo.metadata.application;

import java.util.*;
import org.bson.Document;
import org.springframework.web.client.RestClientResponseException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.martinia.indigo.metadata.application.reviews.ReviewPageGuard.AccessRestrictedException;

/** Diagnostics scoped to one synchronous metadata command; never persist exception messages or URLs. */
public final class ProviderDiagnostics {
    private static final ThreadLocal<List<Document>> CURRENT = new ThreadLocal<>();
    private ProviderDiagnostics() {}
    public static void begin() { CURRENT.set(new ArrayList<>()); }
    public static List<Document> finish() {
        var items = CURRENT.get();
        CURRENT.remove();
        return items == null ? List.of() : List.copyOf(items);
    }
    public static String record(String provider, String operation, Throwable error) {
        String code = "PROVIDER_ERROR";
        Integer status = null;
        java.time.Instant retryAt = null;
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof RestClientResponseException http) status = http.getStatusCode().value();
            if (cause instanceof FailingHttpStatusCodeException http) status = http.getStatusCode();
            if (cause instanceof java.net.SocketTimeoutException || cause instanceof java.net.http.HttpTimeoutException
                    || cause instanceof java.util.concurrent.TimeoutException) code = "TIMEOUT";
            else if (cause instanceof java.net.UnknownHostException || cause instanceof java.net.ConnectException) code = "CONNECTION";
            else if (cause instanceof AccessRestrictedException restricted) {
                retryAt = restricted.retryAt();
                code = retryAt == null ? "ACCESS_RESTRICTED" : "PAUSED";
            }
            else if (cause instanceof com.fasterxml.jackson.core.JsonProcessingException) code = "INVALID_RESPONSE";
        }
        if (status != null) code = switch (status) {
            case 429 -> "RATE_LIMIT";
            case 401, 403 -> "ACCESS_RESTRICTED";
            default -> status >= 500 ? "UNAVAILABLE" : "HTTP_ERROR";
        };
        String message = switch (code) {
            case "PAUSED" -> "Proveedor en pausa preventiva. No se realizan peticiones hasta que termine la pausa.";
            case "TIMEOUT" -> "Tiempo de espera agotado. Puedes reintentar más tarde.";
            case "CONNECTION" -> "No se pudo conectar. Revisa la conexión y la configuración del servicio.";
            case "ACCESS_RESTRICTED" -> "Acceso restringido: puede requerir autenticación o CAPTCHA. No se intentará eludirlo.";
            case "RATE_LIMIT" -> "Límite de peticiones alcanzado. Espera antes de reintentar.";
            case "UNAVAILABLE" -> "El servicio no está disponible temporalmente. Reintenta más tarde.";
            case "HTTP_ERROR" -> "El servicio rechazó la petición. Revisa su configuración.";
            case "INVALID_RESPONSE" -> "La respuesta no tiene el formato esperado. Puede haber cambiado el proveedor.";
            default -> "No se pudo completar la operación. Consulta el log para el detalle técnico.";
        };
        Document detail = new Document("provider", provider).append("operation", operation).append("code", code)
                .append("message", message).append("httpStatus", status).append("retryAt", retryAt == null ? null : Date.from(retryAt));
        var items = CURRENT.get();
        if (items != null && items.size() < 20 && !items.contains(detail)) items.add(detail);
        return provider + " — " + operation + ": " + message;
    }
}
