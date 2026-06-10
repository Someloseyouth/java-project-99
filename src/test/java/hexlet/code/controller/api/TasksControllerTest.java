package hexlet.code.controller.api;


import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TasksControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskMapper taskMapper;

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

    private Task testTask;
    private TaskStatus testStatus;
    private User testAssignee;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        TaskStatus testStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testStatus);

        User testAssignee = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testAssignee);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testStatus);
        testTask.setAssignee(testAssignee);
        taskRepository.save(testTask);

        this.testStatus = testStatus;
        this.testAssignee = testAssignee;
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThat(body).contains(testTask.getName());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/tasks/" + testTask.getId()).with(jwt());
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();
        assertThat(body).contains(testTask.getName());
    }

    @Test
    public void testCreate() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskModel()).create();
        data.setTaskStatus(testStatus);
        data.setAssignee(testAssignee);

        var dto = taskMapper.map(data);

        var request = post("/api/tasks").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isCreated());

        var task = taskRepository.findByName(data.getName()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(data.getName());
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new TaskUpdateDTO();
        data.setTitle(JsonNullable.of("Task"));

        var request = put("/api/tasks/" + testTask.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isOk());

        var task = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(task.getName()).isEqualTo("Task");
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/tasks/" + testTask.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskModel()).create();
        data.setTaskStatus(testStatus);
        data.setAssignee(testAssignee);

        var dto = taskMapper.map(data);

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    public void testFilterByStatus() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(jwt())
                .param("status", testStatus.getSlug()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains(testTask.getName());
    }

    @Test void testFilterByAssigneeId() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(jwt())
                .param("assigneeId", String.valueOf(testAssignee.getId())))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains(testTask.getName());
    }
}
