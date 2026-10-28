package com.example.bakeryshop.config;

import com.example.bakeryprojects.repository.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminAccessConfig implements WebMvcConfigurer {
    private final AdminUserRepository adminUserRepository;

    public AdminAccessConfig(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAccessInterceptor(adminUserRepository));
    }

    private static final class AdminAccessInterceptor implements HandlerInterceptor {
        private final AdminUserRepository adminUserRepository;

        private AdminAccessInterceptor(AdminUserRepository adminUserRepository) {
            this.adminUserRepository = adminUserRepository;
        }

        @Override
        public boolean preHandle(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler) throws Exception {
            String path = request.getRequestURI();
            if (!requiresAdmin(path, request.getMethod())) {
                return true;
            }

            Object adminId = request.getSession(false) == null
                    ? null
                    : request.getSession(false).getAttribute("loggedInAdminId");
            if (adminId instanceof Long id && adminUserRepository.existsById(id)) {
                return true;
            }

            if (path.startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Administrator login required.");
            } else {
                response.sendRedirect("/loging.html");
            }
            return false;
        }

        private static boolean requiresAdmin(String path, String method) {
            if (path.startsWith("/adminpages/") || path.startsWith("/admin/")
                    || path.startsWith("/admin/items/")
                    || path.startsWith("/api/admin/") || path.startsWith("/delivery/update/")
                    || path.startsWith("/delivery/delete/")) {
                return true;
            }
            if (path.equals("/adminDashboard") || path.equals("/adminItems") || path.equals("/addItem")
                    || path.equals("/customOrders") || path.equals("/bakeryOrders") || path.equals("/adminInvoices")
                    || path.equals("/adminPayments") || path.equals("/adminRefunds")
                    || path.equals("/admin-refund-management")
                    || path.equals("/users") || path.equals("/userManagement")
                    || path.equals("/adminindex.html")
                    || path.equals("/adminitem") || path.equals("/adminitem.html")
                    || path.equals("/custome_order") || path.equals("/custome_orders")
                    || path.equals("/custome_orders.html") || path.equals("/admin-orders.html")
                    || path.equals("/admin-payments.html") || path.equals("/adminprofile.html")
                    || path.equals("/adminuserlist.html")) {
                return true;
            }
            if (path.equals("/api/cakeBookings") || path.startsWith("/api/cakeBookings/")) {
                return true;
            }
            if (path.equals("/api/orders") && "GET".equals(method)) {
                return true;
            }
            if (path.matches("/api/orders/\\d+/status") && "PUT".equals(method)) {
                return true;
            }
            if (path.equals("/api/payments") && "GET".equals(method)) {
                return true;
            }
            if ((path.matches("/api/payments/\\d+/status") && "PUT".equals(method))
                    || (path.matches("/api/payments/\\d+") && "DELETE".equals(method))) {
                return true;
            }
            if ((path.equals("/api/refund") && "GET".equals(method))
                    || (path.matches("/api/refund/\\d+") && ("PUT".equals(method) || "DELETE".equals(method)))) {
                return true;
            }
            return (path.equals("/api/cake-options") || path.matches("/api/cake-options/\\d+"))
                    && !"GET".equals(method);
        }
    }
}
