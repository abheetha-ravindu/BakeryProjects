package com.example.bakeryshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bakeryshop.Admin.model.item;
import com.example.bakeryshop.Admin.services.ItemPopularityService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.items.file=target/test-data/items.json",
        "app.items.images=target/test-data/images"
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
