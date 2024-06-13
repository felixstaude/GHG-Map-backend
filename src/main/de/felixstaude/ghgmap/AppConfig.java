package de.felixstaude.ghgmap;

import de.felixstaude.ghgmap.file.ImageProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ImageProcessor imageProcessor() {
        return new ImageProcessor();
    }
}
