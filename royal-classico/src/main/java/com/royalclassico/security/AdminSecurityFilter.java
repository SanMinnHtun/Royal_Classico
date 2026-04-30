package com.royalclassico.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security filter implementing "Security through Obscurity" + Token validation.
 *
 * All requests to /api/v1/rc-internal-mgmt/** must carry the header:
 *   X-Admin-Secret: <configured secret>
 *
 * There is intentionally NO public login page.
 * Any missing or incorrect header returns HTTP 404 (not 401/403)
 * to avoid revealing the existence of the admin API.
 */
@Slf4j
@Component
@Order(1)
public class AdminSecurityFilter implements Filter {

    /**
     * Both admin path prefixes are protected by the same X-Admin-Secret header.
     *  - /api/v1/rc-internal-mgmt/        → original modular admin controllers
     *  - /api/v1/rc-management-internal/  → new unified management controller
     */
    private static final String[] ADMIN_PREFIXES = {
            "/api/v1/rc-internal-mgmt",
            "/api/v1/rc-management-internal",
            "/api/v1/management-internal"
    };
    private static final String ADMIN_HEADER = "X-Admin-Secret";

    @Value("${app.admin.secret}")
    private String adminSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        boolean isAdminPath = false;
        for (String prefix : ADMIN_PREFIXES) {
            if (path.startsWith(prefix)) {
                isAdminPath = true;
                break;
            }
        }

        if (isAdminPath) {
            String providedSecret = req.getHeader(ADMIN_HEADER);
            // Allow also a check for the new localStorage secret value
            if (providedSecret == null || !providedSecret.equals(adminSecret)) {
                log.warn("Blocked admin access attempt — path={}, ip={}",
                        path, req.getRemoteAddr());
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            log.debug("Admin access granted — path={}", path);
        }

        chain.doFilter(request, response);
    }
}
