package br.lucas.com.service_orders.integration;

import br.lucas.com.service_orders.infrastructure.adapter.out.JpaServiceOrderRepository;
import br.lucas.com.service_orders.infrastructure.adapter.out.ServiceOrderEntity;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceOrderIntegrationTest {

    private static final String BASE_URL = "/v1/service-order-management/service-orders";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaServiceOrderRepository jpaServiceOrderRepository;

    @BeforeEach
    void cleanDatabase() {
        jpaServiceOrderRepository.deleteAll();
    }

    @Test
    void shouldCreateScheduleAndSoftDeleteServiceOrder() throws Exception {
        MvcResult created = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "protocol": "OS-2026-0001",
                                  "customerName": "Maria Souza",
                                  "customerDocument": "12345678901",
                                  "type": "INSTALLATION",
                                  "notes": "Cliente prefere periodo da manha"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.protocol").value("OS-2026-0001"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")))
                .andReturn();
        Long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();
        assertThat(created.getResponse().getHeader("Location")).endsWith(BASE_URL + "/" + id);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Maria Souza"))
                .andExpect(jsonPath("$.customerDocument").value("12345678901"))
                .andExpect(jsonPath("$.type").value("INSTALLATION"))
                .andExpect(jsonPath("$.notes").value("Cliente prefere periodo da manha"));

        String scheduledDate = LocalDate.now().plusDays(3).toString();
        mockMvc.perform(patch(BASE_URL + "/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "SCHEDULED", "scheduledDate": "%s" }
                                """.formatted(scheduledDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.scheduledDate").value(scheduledDate));

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ordem de servico nao encontrada: id=" + id + "."));

        ServiceOrderEntity row = jpaServiceOrderRepository.findById(id).orElseThrow();
        assertThat(row.getDeletedAt()).isNotNull();
        assertThat(row.getStatus().name()).isEqualTo("SCHEDULED");
        assertThat(row.getScheduledDate()).isEqualTo(LocalDate.parse(scheduledDate));
    }
}
