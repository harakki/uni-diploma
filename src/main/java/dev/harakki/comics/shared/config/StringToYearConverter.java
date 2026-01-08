package dev.harakki.comics.shared.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
public class StringToYearConverter implements Converter<String, Year> {

    @Override
    public Year convert(String source) {
        try {
            return Year.parse(source);
        } catch (Exception e) {
            return null;
        }
    }

}
