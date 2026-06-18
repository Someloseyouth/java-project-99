package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskFilter;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    private final TaskStatusRepository taskStatusRepository;

    private final UserRepository userRepository;

    private final LabelRepository labelRepository;

    private final TaskMapper taskMapper;

    private final TaskSpecification taskSpecification;

    @Override
    public List<TaskDTO> getAll(TaskFilter filter) {
        var spec = taskSpecification.build(filter);
        return taskRepository.findAll(spec).stream()
                .map(taskMapper::map)
                .toList();
    }

    @Override
    public TaskDTO create(TaskCreateDTO taskData) {
        var task = taskMapper.map(taskData);

        TaskStatus status = taskStatusRepository.findBySlug(taskData.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Status " + taskData.getStatus()
                        + " not found"));
        task.setTaskStatus(status);

        if (taskData.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskData.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User " + taskData.getAssigneeId()
                            + " not found"));
            task.setAssignee(assignee);
        }

        if (taskData.getTaskLabelIds() != null) {
            task.setLabels(resolveLabels(taskData.getTaskLabelIds()));
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Override
    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.map(task);
    }

    @Override
    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        taskMapper.update(taskData, task);

        if (taskData.getAssigneeId() != null && taskData.getAssigneeId().isPresent()) {
            var assigneeId = taskData.getAssigneeId().get();
            if (assigneeId == null) {
                // явно удалили исполнителя
                task.setAssignee(null);
            } else {
                User assignee = userRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User " + assigneeId + " not found"));
                task.setAssignee(assignee);
            }
        }

        if (taskData.getStatus() != null && taskData.getStatus().isPresent()) {
            var statusSlug = taskData.getStatus().get();
            if (statusSlug == null) {
                throw new IllegalArgumentException("Task status cannot be null");
            }
            TaskStatus status = taskStatusRepository.findBySlug(statusSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Status " + statusSlug + " not found"));
            task.setTaskStatus(status);
        }

        if (taskData.getTaskLabelIds() != null && taskData.getTaskLabelIds().isPresent()) {
            task.setLabels(resolveLabels(taskData.getTaskLabelIds().get()));
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Override
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    private Set<Label> resolveLabels(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Label> found = labelRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more labels not found");
        }
        return new HashSet<>(found);
    }
}
