package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.TestConfig;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
public class TaskStatusesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;

    @BeforeEach
    public void setUp() {
        taskStatusRepository.deleteAll();

        var draft = new TaskStatus();
        draft.setName("Draft");
        draft.setSlug("draft");
        taskStatusRepository.save(draft);

        var published = new TaskStatus();
        published.setName("Published");
        published.setSlug("published");
        taskStatusRepository.save(published);
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/task_statuses").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).contains("draft");
        assertThat(body).contains("published");
    }

    @Test
    public void testShow() throws Exception {
        var status = taskStatusRepository.findBySlug("draft").orElseThrow();

        var request = get("/api/task_statuses/" + status.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("draft");
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName("To review");
        dto.setSlug("to_review");

        var request = post("/api/task_statuses").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(dto.getSlug()).orElse(null);

        assertNotNull(taskStatus);
        assertThat(taskStatus.getName()).isEqualTo(dto.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(dto.getSlug());
    }

    @Test
    public void testUpdate() throws Exception {
        var status = taskStatusRepository.findBySlug("published").orElseThrow();
        var data = new HashMap<>();
        data.put("name", "New name");
        data.put("slug", "new_slug");

        var request = put("/api/task_statuses/" + status.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(status.getId()).orElseThrow();
        assertThat(taskStatus.getName()).isEqualTo("New name");
        assertThat(taskStatus.getSlug()).isEqualTo("new_slug");
    }

    @Test
    public void testDelete() throws Exception {
        var status = taskStatusRepository.findBySlug("draft").orElseThrow();
        var request = delete("/api/task_statuses/" + status.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(taskStatusRepository.findById(status.getId()).isEmpty());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName("Unauthorized");
        dto.setSlug("unauthorized");

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
