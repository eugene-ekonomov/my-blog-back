package ru.ekonomov.blogback.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ekonomov.blogback.config.RootConfiguration;
import ru.ekonomov.blogback.config.WebConfig;
import ru.ekonomov.blogback.repository.AbstractIntegrationTest;

@SpringJUnitWebConfig({RootConfiguration.class, WebConfig.class})
public abstract class AbstractWebTest extends AbstractIntegrationTest {

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    JdbcTemplate jdbcTemplate;

    MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jdbcTemplate.update("truncate posts, comments restart identity cascade");
    }
}
