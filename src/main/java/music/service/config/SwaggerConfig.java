package music.service.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Music Service API")
                        .version("1.0")
                        .description("API for managing music tracks, albums, and playlists"));
    }
}