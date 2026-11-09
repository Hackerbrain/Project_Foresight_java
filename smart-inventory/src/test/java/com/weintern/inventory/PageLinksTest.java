package com.weintern.inventory;

import com.weintern.inventory.web.PageLinks;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageLinksTest {

    @Test
    void keepsFiltersAndEndsReadyForPageParameter() {
        assertThat(PageLinks.of("/products", "q", "pen", "low", true)).isEqualTo("/products?q=pen&low=true&");
    }

    @Test
    void encodesSpecialCharacters() {
        assertThat(PageLinks.of("/products", "q", "a&b=c")).doesNotContain("a&b=c");
    }
}
