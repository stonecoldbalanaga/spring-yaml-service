package com.example.yamlservice;

import com.example.yamlservice.exception.YamlValidationException;
import com.example.yamlservice.model.AppValues;
import com.example.yamlservice.service.AppValuesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "app.yaml.file-path=appvalues.yaml")
class AppValuesServiceTest {

    @Autowired
    private AppValuesService service;

    @Test
    void loadsAndValidatesSuccessfully() {
        AppValues result = service.loadAndValidate();

        assertThat(result).isNotNull();
        assertThat(result.getEntries()).isNotEmpty();
        assertThat(result.getEntries()).containsKey("app.name");
    }

    @Test
    void getValueReturnsCorrectValue() {
        String value = service.getValue("app.name");
        assertThat(value).isEqualTo("YamlMicroservice");
    }

    @Test
    void getValueThrowsForUnknownKey() {
        assertThatThrownBy(() -> service.getValue("no.such.key"))
                .isInstanceOf(YamlValidationException.class)
                .hasMessageContaining("not found in AppValues");
    }
}
