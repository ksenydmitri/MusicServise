package music.service.config;

import music.service.interceptors.VisitCounterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private VisitCounterInterceptor visitCounterInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(visitCounterInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/visits/**"); // Чтобы не учитывать свои же запросы

    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Разрешаем запросы на всех маршрутах
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Разрешённые HTTP-методы
                        .allowedHeaders("*") // Все заголовки разрешены
                        .allowCredentials(true); // Разрешаем передачу cookies
            }
        };
    }



}