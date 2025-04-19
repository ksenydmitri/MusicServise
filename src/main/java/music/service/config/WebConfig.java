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
                registry.addMapping("/**") // Настройка для всех маршрутов
                        .allowedOrigins("http://localhost:3000") // Разрешить запросы с вашего фронтенд-домена
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH"); // Указать разрешенные методы
            }
        };
    }

}