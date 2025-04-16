package music.service.config;

import music.service.interceptors.VisitCounterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
}