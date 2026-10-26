package com.example.demo.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUsernameResolverTest {

    private final CurrentUsernameResolver resolver = new CurrentUsernameResolver();

    @SuppressWarnings("unused")
    void sample(@CurrentUsername String annotated, String plain) {
    }

    private MethodParameter param(int index) throws NoSuchMethodException {
        return new MethodParameter(getClass().getDeclaredMethod("sample", String.class, String.class), index);
    }

    @Test
    void supportsOnlyAnnotatedParameter() throws Exception {
        assertThat(resolver.supportsParameter(param(0))).isTrue();
        assertThat(resolver.supportsParameter(param(1))).isFalse();
    }

    @Test
    void resolvesPrincipalName() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(() -> "alice");

        Object value = resolver.resolveArgument(param(0), null, new ServletWebRequest(request), null);

        assertThat(value).isInstanceOf(String.class).isEqualTo("alice");
    }

    @Test
    void resolvesAnonymousWithoutPrincipal() throws Exception {
        Object value = resolver.resolveArgument(param(0), null, new ServletWebRequest(new MockHttpServletRequest()), null);

        assertThat(value).isEqualTo("anonymous");
    }
}
