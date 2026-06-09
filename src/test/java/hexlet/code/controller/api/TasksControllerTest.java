package hexlet.code.controller.api;

import hexlet.code.config.TestConfig;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
public class TasksControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    private TaskStatus draftStatus;
    private User assignee;
    private Task task;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        draftStatus = new TaskStatus();
        draftStatus.setName("Draft");
        draftStatus.setSlug("draft");
        taskStatusRepository.save(draftStatus);

        assignee = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(assignee);

        task = new Task();
        task.setName("Test task");
        task.setDescription("Test description");
        task.setIndex(100);
        task.setTaskStatus(draftStatus);
        task.setAssignee(assignee);
        taskRepository.save(task);
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("Test task");
        assertThat(body).contains("draft");
        assertThat(body).contains(assignee.getId().toString());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/tasks/" + task.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("Test task");
        assertThat(body).contains("draft");
        assertThat(body).contains(assignee.getId().toString());
    }

    @Test
    public void testCreate() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setIndex(200);
        dto.setAssigneeId(assignee.getId());
        dto.setTitle("New task");
        dto.setContent("New Content");
        dto.setStatus("draft");

        var request = post("/api/tasks").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).contains("New task");
        assertThat(body).contains("draft");
        assertThat(body).contains(assignee.getId().toString());

        var savedTask = taskRepository.findAll()
                .stream()
                .filter(t -> t.getName().equals("New task"))
                .findFirst()
                .orElse(null);

        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getName()).isEqualTo("New task");
        assertThat(savedTask.getTaskStatus().getSlug()).isEqualTo("draft");
        assertThat(savedTask.getAssignee().getId()).isEqualTo(assignee.getId());
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new TaskUpdateDTO();
        data.setTitle(JsonNullable.of("Updated task"));
        data.setContent(JsonNullable.of("Updated content"));

        var request = put("/api/tasks/" + task.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo("Updated task");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated content");
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo("draft");
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(assignee.getId());
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/tasks/" + task.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setIndex(300);
        dto.setAssigneeId(assignee.getId());
        dto.setTitle("Unauthorized task");
        dto.setContent("Unauthorized content");
        dto.setStatus("draft");

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
