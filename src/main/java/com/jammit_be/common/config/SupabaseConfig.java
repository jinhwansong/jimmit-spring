package com.jammit_be.common.config;

import com.jammit_be.common.properties.SupabaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("!local") // local 프로필에서는 Supabase Storage를 사용하지 않음
@RequiredArgsConstructor
public class SupabaseConfig {

    private final SupabaseProperties supabaseProperties;

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseProperties.getUrl())
                .defaultHeader("apikey", supabaseProperties.getServiceRoleKey())
                .defaultHeader("Authorization", "Bearer " + supabaseProperties.getServiceRoleKey())
                .build();
    }
}
