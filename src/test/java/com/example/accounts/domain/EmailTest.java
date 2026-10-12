package com.example.accounts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void acceptsAValidAddress() {
        assertThat(new Email("alice@example.com").value()).isEqualTo("alice@example.com");
    }

    @Test
    void rejectsAnAddressWithNoAtSign() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Email("not-an-email"));
    }

    @Test
    void rejectsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Email(null));
    }
}
