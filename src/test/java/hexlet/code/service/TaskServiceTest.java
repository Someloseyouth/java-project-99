package hexlet.code.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

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

        var status = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(status);

        var assignee = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(assignee);

        var task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        taskRepository.save(task);

        this.testStatus = status;
        this.testAssignee = assignee;
        this.testTask = task;
    }

    @Test
    public void testCreate() {
        var dto = new TaskCreateDTO();
        dto.setTitle("Task from service");
        dto.setContent("Some description");
        dto.setStatus(testStatus.getSlug());
        dto.setAssigneeId(testAssignee.getId());
        dto.setLabelIds(List.of());

        var createdDto = taskService.create(dto);

        var task = taskRepository.findById(createdDto.getId()).orElseThrow();
        assertThat(task.getName()).isEqualTo("Task from service");
        assertThat(task.getDescription()).isEqualTo("Some description");
        assertThat(task.getTaskStatus().getId()).isEqualTo(testStatus.getId());
        assertThat(task.getAssignee().getId()).isEqualTo(testAssignee.getId());
    }

    @Test
    public void testCreateWithUnknownStatus() {
        var dto = new TaskCreateDTO();
        dto.setTitle("Broken");
        dto.setContent("Broken");
        dto.setStatus("unknown_status");
        dto.setAssigneeId(testAssignee.getId());
        dto.setLabelIds(List.of());

        assertThrows(RuntimeException.class, () -> taskService.create(dto));
        assertThat(taskRepository.findByName("Broken")).isEmpty();
    }

    @Test
    public void testUpdateTitle() {
        var updateDto = new TaskUpdateDTO();
        updateDto.setTitle(JsonNullable.of("New title"));

        var updatedDto = taskService.update(updateDto, testTask.getId());

        var task = taskRepository.findById(updatedDto.getId()).orElseThrow();
        assertThat(task.getName()).isEqualTo("New title");
    }

    @Test
    public void testUpdateStatusAndAssignee() {
        var newStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newStatus);

        var newAssignee = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newAssignee);

        var updateDto = new TaskUpdateDTO();
        updateDto.setStatus(JsonNullable.of(newStatus.getSlug()));
        updateDto.setAssigneeId(JsonNullable.of(newAssignee.getId()));

        var updatedDto = taskService.update(updateDto, testTask.getId());

        var task = taskRepository.findById(updatedDto.getId()).orElseThrow();
        assertThat(task.getTaskStatus().getId()).isEqualTo(newStatus.getId());
        assertThat(task.getAssignee().getId()).isEqualTo(newAssignee.getId());
    }

    @Test
    public void testUpdateClearAssignee() {
        var updateDto = new TaskUpdateDTO();
        updateDto.setAssigneeId(JsonNullable.of(null));

        var updatedDto = taskService.update(updateDto, testTask.getId());

        var task = taskRepository.findById(updatedDto.getId()).orElseThrow();
        assertThat(task.getAssignee()).isNull();
    }
}