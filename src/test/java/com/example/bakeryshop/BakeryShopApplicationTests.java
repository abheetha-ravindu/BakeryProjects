package com.example.bakeryshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bakeryshop.Admin.model.item;
import com.example.bakeryshop.Admin.services.ItemPopularityService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bakery-test;DB_CLOSE_DELAY=-1",
        "app.items.file=target/test-data/items.json",
        "app.items.images=target/test-data/images",
        "app.cake-reference-images=target/test-data/reference-images"
})
@AutoConfigureMockMvc
class BakeryShopApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemPopularityService itemPopularityService;

    @Test
    void contextLoads() {
    }

    @Test
    void adminPageRenders() throws Exception {
        mockMvc.perform(get("/adminitem"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Saved items")));

        mockMvc.perform(get("/adminitem.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Saved items")));
    }

    @Test
    void customOrdersRoutesOpenStaticAdminPage() throws Exception {
        mockMvc.perform(get("/custome_order"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/custome_orders.html"));

        mockMvc.perform(get("/custome_orders"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", "/custome_orders.html"));

        mockMvc.perform(get("/custome_orders.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Custom Cake Orders")))
                .andExpect(content().string(containsString("/api/cakeBookings")));
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
        mockMvc.perform(post("/api/admin/updateBookingStatus")
                        .contentType("application/json")
                        .content("""
                                {"bookingId":%s,"status":"confirmed"}
                                """.formatted(bookingId.group(1))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"CONFIRMED\"")));

        mockMvc.perform(get("/api/cakeBookings"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"cakeMessage\":\"Happy Birthday\"")))
                .andExpect(content().string(containsString("\"status\":\"CONFIRMED\"")));
    }

    @Test
    void cakeOptionApiReturnsTypesUsedByTheBuilder() throws Exception {
        mockMvc.perform(post("/api/cake-options")
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
}
