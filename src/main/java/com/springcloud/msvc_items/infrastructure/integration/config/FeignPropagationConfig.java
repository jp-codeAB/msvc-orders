package com.springcloud.msvc_items.infrastructure.integration.config;

import com.springcloud.msvc_items.infrastructure.security.AuthUser;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;

@Configuration
public class FeignPropagationConfig {


    @Bean
    public RequestInterceptor requestS2SHeadersInterceptor() {
        return new RequestInterceptor() {
            private static final String USER_ID_HEADER = "X-User-ID";
            private static final String USER_ROLES_HEADER = "X-User-Roles";
            private static final String USER_EMAIL_HEADER = "X-User-Email";
            @Override
            public void apply(RequestTemplate template) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                    AuthUser authUser = (AuthUser) authentication.getPrincipal();

                    template.header(USER_ID_HEADER, authUser.getId().toString());
                    template.header(USER_EMAIL_HEADER, authUser.getEmail());

                    String roles = authUser.getAuthorities().stream()
                            .map(a -> a.getAuthority().replace("ROLE_", ""))
                            .collect(Collectors.joining(","));
                    template.header(USER_ROLES_HEADER, roles);
                }
            }
        };
    }
}
