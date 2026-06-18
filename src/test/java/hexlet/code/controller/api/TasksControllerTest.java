package hexlet.code.controller.api;


import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    private Task testTask;
    private TaskStatus testStatus;
    private User testAssignee;
    private Label testLabel;


    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        this.testStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(this.testStatus);

        this.testAssignee = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(this.testAssignee);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(this.testStatus);
        testTask.setAssignee(this.testAssignee);
        taskRepository.save(testTask);
        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);
    }

    @Test
    @Transactional
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        List<TaskDTO> taskDTOs = om.readValue(body, new TypeReference<>() {
        });
        var expectedDTOs = taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();

        assertThat(taskDTOs).isNotEmpty();
        assertThat(taskDTOs)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedDTOs);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/tasks/" + testTask.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        TaskDTO taskDTO = om.readValue(body, TaskDTO.class);
        var expectedDTO = taskMapper.map(testTask);

        assertThat(taskDTO)
                .usingRecursiveComparison()
                .isEqualTo(expectedDTO);
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

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var response = om.readValue(
                result.getResponse().getContentAsString(), TaskDTO.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo(data.getName());
        assertThat(response.getStatus()).isEqualTo(testStatus.getSlug());
        assertThat(response.getAssigneeId()).isEqualTo(testAssignee.getId());
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new TaskUpdateDTO();
        data.setTitle(JsonNullable.of("Task"));

        var request = put("/api/tasks/" + testTask.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var response = om.readValue(
                result.getResponse().getContentAsString(), TaskDTO.class);

        assertThat(response.getTitle()).isEqualTo("Task");
        assertThat(response.getId()).isEqualTo(testTask.getId());
        assertThat(response.getStatus()).isEqualTo(testStatus.getSlug());
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

        List<TaskDTO> tasks = om.readValue(body, new TypeReference<>() {
        });
        assertThat(tasks).isNotEmpty();
        assertThat(tasks).allMatch(t -> t.getStatus().equals(testStatus.getSlug()));
    }

    @Test
    public void testFilterByAssigneeId() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(jwt())
                        .param("assigneeId", String.valueOf(testAssignee.getId())))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<TaskDTO> tasks = om.readValue(body, new TypeReference<>() {
        });
        assertThat(tasks).isNotEmpty();
        assertThat(tasks).allMatch(t -> t.getAssigneeId().equals(testAssignee.getId()));
    }

    @Test
    public void testCreateWithLabels() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskModel()).create();
        data.setTaskStatus(testStatus);
        data.setAssignee(testAssignee);

        var dto = taskMapper.map(data);
        dto.setTaskLabelIds(Set.of(testLabel.getId()));

        var request = post("/api/tasks").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var taskDTO = om.readValue(body, TaskDTO.class);
        assertThat(taskDTO.getTaskLabelIds()).contains(testLabel.getId());
    }

    @Test
    public void testCreateWithNonExistentLabel() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskModel()).create();
        data.setTaskStatus(testStatus);

        var dto = taskMapper.map(data);
        dto.setTaskLabelIds(Set.of(999999L));

        var request = post("/api/tasks").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isNotFound());
    }
}
