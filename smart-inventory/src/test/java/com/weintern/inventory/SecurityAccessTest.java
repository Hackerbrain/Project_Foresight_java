package com.weintern.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies server-side authorization rules (acceptance criterion: unauthorized actions are rejected). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired MockMvc mvc;

    @Test
    void anonymousUsersAreSentToLogin() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "INVENTORY_STAFF")
    void inventoryStaffCanViewDashboardAndProducts() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/products")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "INVENTORY_STAFF")
    void inventoryStaffCannotCreatePurchaseOrders() throws Exception {
        mvc.perform(get("/purchase-orders/new")).andExpect(status().isForbidden());
        mvc.perform(post("/purchase-orders").with(csrf()).param("supplierId", "1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PURCHASING_STAFF")
    void purchasingStaffCannotChangeStockOrProducts() throws Exception {
        mvc.perform(get("/inventory/transaction")).andExpect(status().isForbidden());
        mvc.perform(get("/products/new")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PURCHASING_STAFF")
    void purchasingStaffCanOpenNewPurchaseOrderForm() throws Exception {
        mvc.perform(get("/purchase-orders/new")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "INVENTORY_STAFF")
    void nonAdminsCannotManageUsers() throws Exception {
        mvc.perform(get("/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanManageUsers() throws Exception {
        mvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/categories").param("name", "Tools")).andExpect(status().isForbidden());
    }
}
