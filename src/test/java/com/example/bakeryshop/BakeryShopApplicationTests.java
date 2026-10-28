package com.example.bakeryshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bakeryshop.Admin.model.item;
import com.example.bakeryshop.Admin.services.ItemPopularityService;
import com.example.bakeryshop.billing.RefundRequest;
import com.bakery.bakeryplatform.model.order.Order;
import com.bakery.bakeryplatform.repository.order.OrderRepository;
import com.example.bakeryprojects.model.Customer;
import com.example.bakeryprojects.model.AdminUser;
import com.example.bakeryprojects.repository.AdminUserRepository;
import com.example.bakeryprojects.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bakery-test;DB_CLOSE_DELAY=-1",
        "app.items.file=target/test-data/items.json",
        "app.items.images=target/test-data/images",
        "app.cake-reference-images=target/test-data/reference-images",
        "app.admin.username=test-admin",
        "app.admin.email=test-admin@example.com",
        "app.admin.password=test-admin-password"
})
@AutoConfigureMockMvc
class BakeryShopApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemPopularityService itemPopularityService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.example.bakeryshop.billing.RefundRepository refundRepository;

    private MockHttpSession adminSession() {
        AdminUser admin = adminUserRepository.findByUsername("test-admin");
        assertTrue(admin != null, "The configured test administrator should exist.");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdminId", admin.getId());
        return session;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void adminPageRenders() throws Exception {
        mockMvc.perform(get("/adminpages/adminitem.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Saved items")));

        mockMvc.perform(get("/adminitem.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Saved items")));

        mockMvc.perform(get("/adminRefunds").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/admin-refund-management.html"));

        mockMvc.perform(get("/adminpages/admin-refund-management.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Refund requests")));
    }

    @Test
    void profilePageLoadsAndUpdatesCurrentSessionAccount() throws Exception {
        Customer customer = new Customer();
        customer.setUsername("Profile Customer");
        customer.setEmail("profile-" + System.nanoTime() + "@example.com");
        customer.setPassword("test-password");
        customer.setPhoneNumber("0770000000");
        customer = userRepository.save(customer);

        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("loggedInUserId", customer.getId());

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/profile").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Profile Customer"))
                .andExpect(jsonPath("$.email").value(customer.getEmail()));

        mockMvc.perform(put("/api/profile")
                        .session(customerSession)
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Updated Profile Customer",
                                  "phoneNumber": "0771111111",
                                  "address": "12 Main Street",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Profile Customer"))
                .andExpect(jsonPath("$.address").value("12 Main Street"));

        Customer savedCustomer = (Customer) userRepository.findById(customer.getId()).orElseThrow();
        assertEquals("Updated Profile Customer", savedCustomer.getUsername());
        assertEquals("0771111111", savedCustomer.getPhoneNumber());

        mockMvc.perform(get("/profileupdate"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/profileupdate.html"));

        mockMvc.perform(get("/profileupdate.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/billing/my-invoices.html")));
    }

    @Test
    void billingRoutesRedirectToExistingStaticPages() throws Exception {
        mockMvc.perform(get("/my-invoices"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/billing/my-invoices.html"));

        mockMvc.perform(get("/invoice-details").param("invoiceId", "7"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/billing/invoice-detail.html?id=7"));

        mockMvc.perform(get("/refund-request-form").param("orderId", "12"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/billing/refund-request-form.html?orderId=12"));
    }

    @Test
    void refundRequestsAreCustomerOwnedAndAdminProcessed() throws Exception {
        Customer customer = new Customer();
        customer.setUsername("refund-customer");
        customer.setEmail("refund-" + System.nanoTime() + "@example.com");
        customer.setPassword("test-password");
        customer = userRepository.save(customer);

        Order order = orderRepository.save(
                new Order(customer.getId(), null, "1 x Celebration Cake", new BigDecimal("45.00")));
        MockHttpSession customerSession = new MockHttpSession();
        customerSession.setAttribute("loggedInUserId", customer.getId());

        Customer otherCustomer = new Customer();
        otherCustomer.setUsername("other-refund-customer");
        otherCustomer.setEmail("other-refund-" + System.nanoTime() + "@example.com");
        otherCustomer.setPassword("test-password");
        otherCustomer = userRepository.save(otherCustomer);
        MockHttpSession otherCustomerSession = new MockHttpSession();
        otherCustomerSession.setAttribute("loggedInUserId", otherCustomer.getId());

        mockMvc.perform(post("/api/refund")
                        .session(otherCustomerSession)
                        .contentType("application/json")
                        .content("""
                                {"orderId":%d,"reason":"Not my order"}
                                """.formatted(order.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/refund")
                        .session(customerSession)
                        .contentType("application/json")
                        .content("""
                                {"orderId":%d,"reason":"Damaged during delivery"}
                                """.formatted(order.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderId").value(order.getId()));

        mockMvc.perform(get("/api/refund"))
                .andExpect(status().isUnauthorized());

        MockHttpSession adminSession = adminSession();
        mockMvc.perform(get("/api/refund").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Damaged during delivery")));

        RefundRequest refund = refundRepository.findAll().stream()
                .filter(request -> order.getId().equals(request.getOrderId()))
                .findFirst()
                .orElseThrow();
        mockMvc.perform(put("/api/refund/" + refund.getId())
                        .session(adminSession)
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(put("/api/refund/" + refund.getId())
                        .session(adminSession)
                        .param("status", "PENDING"))
                .andExpect(status().isBadRequest());

        RefundRequest savedRequest = refundRepository.findById(refund.getId()).orElseThrow();
        assertEquals("APPROVED", savedRequest.getStatus());
    }

    @Test
    void registrationAndLoginCreateHashedAccountsAndSessions() throws Exception {
        String email = "cake-customer-" + System.nanoTime() + "@example.com";
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Cake Customer",
                                  "email": "%s",
                                  "phone": "+94 77 123 4567",
                                  "password": "customer-password",
                                  "returnUrl": "//untrusted.example"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"role\":\"CUSTOMER\"")))
                .andExpect(content().string(containsString("\"redirectUrl\":\"index.html\"")))
                .andReturn();

        assertFalse(registration.getResponse().getContentAsString().contains("customer-password"));
        var savedUser = userRepository.findByEmail(email);
        assertTrue(savedUser != null);
        assertTrue(savedUser.getPassword().startsWith("pbkdf2$"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Duplicate Customer",
                                  "email": "%s",
                                  "phone": "+94 77 123 4567",
                                  "password": "customer-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isConflict());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "customer-password",
                                  "returnUrl": "/orders.html"
                                }
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"redirectUrl\":\"/orders.html\"")))
                .andReturn();
        assertTrue(login.getRequest().getSession(false) != null);
        assertEquals(savedUser.getId(),
                login.getRequest().getSession(false).getAttribute("loggedInUserId"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "customer-password",
                                  "returnUrl": "javascript:alert(1)"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"redirectUrl\":\"index.html\"")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"wrong-password"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPagesAndBookingDataRequireAdministratorSession() throws Exception {
        mockMvc.perform(get("/custome_orders.html"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/loging.html"));

        mockMvc.perform(get("/api/cakeBookings"))
                .andExpect(status().isUnauthorized());

        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"test-admin","password":"test-admin-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"role\":\"ADMIN\"")))
                .andExpect(content().string(containsString("\"redirectUrl\":\"/adminpages/adminindex.html\"")))
                .andReturn();

        MockHttpSession authenticatedAdminSession =
                (MockHttpSession) adminLogin.getRequest().getSession(false);
        assertTrue(authenticatedAdminSession != null);
        mockMvc.perform(get("/api/cakeBookings").session(authenticatedAdminSession))
                .andExpect(status().isOk());
    }

    @Test
    void userManagementIsAdminOnlyAndListsCustomersWithoutPasswords() throws Exception {
        String email = "managed-customer-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Managed Customer",
                                  "email": "%s",
                                  "phone": "+94 77 987 6543",
                                  "password": "customer-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        var customer = userRepository.findByEmail(email);
        assertTrue(customer != null);

        mockMvc.perform(get("/userManagement"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/loging.html"));

        mockMvc.perform(get("/users").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/userManage.html"));

        mockMvc.perform(get("/adminpages/userManage.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User management")))
                .andExpect(content().string(containsString("/api/admin/users")));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/users").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(email)))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("customer-password"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("pbkdf2$"))));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/admin/users/{id}", customer.getId()).session(adminSession()))
                .andExpect(status().isNoContent());
        assertTrue(userRepository.findById(customer.getId()).isEmpty());
    }

    @Test
    void customOrdersRoutesOpenStaticAdminPage() throws Exception {
        mockMvc.perform(get("/adminDashboard").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/adminindex.html"));

        mockMvc.perform(get("/customOrders").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/custome_orders.html"));

        mockMvc.perform(get("/bakeryOrders").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/admin-orders.html"));

        mockMvc.perform(get("/adminPayments").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/admin-payments.html"));

        mockMvc.perform(get("/adminItems").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/adminitem.html"));

        mockMvc.perform(get("/custome_order").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/custome_orders.html"));

        mockMvc.perform(get("/custome_orders").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/custome_orders.html"));

        mockMvc.perform(get("/adminpages/custome_orders.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Custom Cake Orders")))
                .andExpect(content().string(containsString("/api/cakeBookings")));

        mockMvc.perform(get("/adminpages/adminindex.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bakery dashboard")))
                .andExpect(content().string(containsString("href=\"/adminPayments\"")));

        mockMvc.perform(get("/admin-orders.html").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/admin-orders.html"));

        mockMvc.perform(get("/custome_orders.html").session(adminSession()))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/adminpages/custome_orders.html"));
    }

    @Test
    void bakeryOrderAndPaymentPagesLoadTheirConnectedAssets() throws Exception {
        for (String page : List.of(
                "/orders.html",
                "/payment.html",
                "/payments.html",
                "/adminpages/admin-orders.html",
                "/adminpages/admin-payments.html")) {
            var request = get(page);
            if (page.startsWith("/adminpages/")) {
                request.session(adminSession());
            }
            mockMvc.perform(request)
                    .andExpect(status().isOk());
        }

        for (String asset : List.of(
                "/css/style.css",
                "/js/common.js",
                "/js/main.js",
                "/js/payment-common.js",
                "/css/admin-panel.css",
                "/js/orders.js",
                "/js/payment.js",
                "/js/payments.js",
                "/js/admin-orders.js",
                "/js/admin-payments.js")) {
            mockMvc.perform(get(asset))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/css/admin-panel.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".nav-section")))
                .andExpect(content().string(containsString("text-decoration: none")));

        mockMvc.perform(get("/adminpages/admin-orders.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"nav-item active\" href=\"/bakeryOrders\"")));

        mockMvc.perform(get("/adminpages/admin-payments.html").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"nav-item active\" href=\"/adminPayments\"")));
    }

    @Test
    void customerPagesAreConnectedThroughWebControllerAndUseKamudaBranding() throws Exception {
        Map<String, String> routes = Map.of(
                "/main", "/index.html",
                "/items", "/item.html",
                "/login", "/login.html",
                "/register", "/register.html",
                "/orders", "/orders.html",
                "/payment", "/payment.html",
                "/payments", "/payments.html",
                "/profileupdate", "/profileupdate.html");

        for (var route : routes.entrySet()) {
            mockMvc.perform(get(route.getKey()))
                    .andExpect(status().isFound())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                            .header().string("Location", route.getValue()));

            mockMvc.perform(get(route.getValue()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("kamuda.com")));
        }

        mockMvc.perform(post("/register")
                        .param("fullName", "Route Test Customer")
                        .param("email", "route-test-" + System.nanoTime() + "@example.com")
                        .param("phone", "+94 77 234 5678")
                        .param("password", "customer-password"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/login"));
    }

    @Test
    void customCakeRouteRedirectsToStaticPage() throws Exception {
        mockMvc.perform(get("/custom-cake"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/custom-cake.html"));

        mockMvc.perform(get("/custome_cake"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/custom-cake.html"));

        mockMvc.perform(get("/custom-cake.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Your Custom Cake")));
    }

    @Test
    void customCakeBookingIsSavedWithItsDesignDetails() throws Exception {
        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerName": "Jamie Baker",
                                  "contactNumber": "0771234567",
                                  "customerEmail": "jamie@example.com",
                                  "requiredDate": "2026-11-15",
                                  "preferredTime": "Afternoon",
                                  "occasion": "Birthday",
                                  "shape": "Heart",
                                  "flavor": "Chocolate",
                                  "decoration": "Floral",
                                  "filling": "Vanilla Cream",
                                  "cakeSize": "1.5 kg",
                                  "cakeMessage": "Happy Birthday",
                                  "pickupOrDelivery": "Pickup",
                                  "estimatedPrice": 6800,
                                  "notes": "Less sugar"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"shape\":\"Heart\"")))
                .andExpect(content().string(containsString("\"customerEmail\":\"jamie@example.com\"")))
                .andReturn();

        Matcher bookingId = Pattern.compile("\"bookingId\":(\\d+)")
                .matcher(bookingResult.getResponse().getContentAsString());
        assertTrue(bookingId.find(), "The created booking should include its ID.");
        mockMvc.perform(post("/api/admin/updateBookingStatus").session(adminSession())
                        .contentType("application/json")
                        .content("""
                                {"bookingId":%s,"status":"confirmed"}
                                """.formatted(bookingId.group(1))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"CONFIRMED\"")));

        mockMvc.perform(get("/api/cakeBookings").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"cakeMessage\":\"Happy Birthday\"")))
                .andExpect(content().string(containsString("\"status\":\"CONFIRMED\"")));
    }

    @Test
    void cakeOptionApiReturnsTypesUsedByTheBuilder() throws Exception {
        mockMvc.perform(post("/api/cake-options").session(adminSession())
                        .contentType("application/json")
                        .content("""
                                {"optionType":"FLAVOUR","name":"Vanilla","price":150}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cake-options/available"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"optionType\":\"FLAVOUR\"")))
                .andExpect(content().string(containsString("\"name\":\"Vanilla\"")));
    }

    @Test
    void uploadedCakeReferenceImageCanBeRetrieved() throws Exception {
        byte[] pngSignature = {
                (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile image = new MockMultipartFile(
                "file", "reference.png", "image/png", pngSignature);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/cake-designs/upload").file(image))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"success\":true")))
                .andReturn();
        Matcher imageUrl = Pattern.compile("\"imageUrl\":\"([^\"]+)\"")
                .matcher(uploadResult.getResponse().getContentAsString());
        assertTrue(imageUrl.find(), "Upload response should include the saved image URL.");

        mockMvc.perform(get(imageUrl.group(1)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(pngSignature));
    }

    @Test
    void storefrontItemEndpointsReturnJsonArrays() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/api/items/recommended"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void recommendationsRankAvailableItemsByCartActivity() {
        item mostPopular = new item("popular", "Popular", "", "", "", 1, 0,
                "", "Available", "");
        item lessPopular = new item("less-popular", "Less popular", "", "", "", 1, 0,
                "", "Available", "");
        item unavailable = new item("unavailable", "Unavailable", "", "", "", 1, 0,
                "", "Unavailable", "");

        itemPopularityService.recordAddToCart(lessPopular.getId());
        itemPopularityService.recordAddToCart(mostPopular.getId());
        itemPopularityService.recordAddToCart(mostPopular.getId());
        itemPopularityService.recordAddToCart(unavailable.getId());

        assertEquals(List.of(mostPopular, lessPopular),
                itemPopularityService.getPopularItems(
                        List.of(lessPopular, unavailable, mostPopular), 4));
    }

    @Test
    void cartOrderAndPaymentApisWorkTogether() throws Exception {
        long userId = 77990011L;
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/cart/user/{userId}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/cart")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": %d,
                                  "productId": "CAKE-901",
                                  "productName": "Chocolate Celebration Cake",
                                  "quantity": 2,
                                  "unitPrice": 1250.00
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"quantity\":2")));

        mockMvc.perform(get("/api/cart/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":2500.00")));

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {"userId": %d, "note": "No peanuts"}
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"status\":\"PENDING\"")))
                .andExpect(content().string(containsString("Chocolate Celebration Cake")))
                .andReturn();

        Matcher orderId = Pattern.compile("\"id\":(\\d+)")
                .matcher(orderResult.getResponse().getContentAsString());
        assertTrue(orderId.find(), "The created order should include its ID.");
        String id = orderId.group(1);

        mockMvc.perform(get("/api/cart/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(put("/api/orders/{id}/status", id).session(adminSession())
                        .contentType("application/json")
                        .content("{\"status\":\"PREPARING\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"PREPARING\"")));

        mockMvc.perform(post("/api/payments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "orderId": %s,
                                  "paymentType": "CARD",
                                  "cardHolderName": "Jamie Baker",
                                  "cardNumber": "4111111111111111"
                                }
                                """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"cardLast4\":\"1111\"")))
                .andExpect(content().string(containsString("\"fee\":50.00")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("4111111111111111"))));
    }
}
