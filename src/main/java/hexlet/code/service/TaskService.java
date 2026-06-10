package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    public List<TaskDTO> getAll(String titleCont, Long assigneeId, String status, Long labelId) {
        return taskRepository.findAll().stream()
                .filter(task -> titleCont == null
                        || task.getName().toLowerCase().contains(titleCont.toLowerCase()))
                .filter(task -> assigneeId == null
                        || (task.getAssignee() != null && task.getAssignee().getId().equals(assigneeId)))
                .filter(task -> status == null
                        || (task.getTaskStatus() != null && status.equals(task.getTaskStatus().getSlug())))
                .filter(task -> labelId == null
                        || task.getLabels().stream().anyMatch(label -> label.getId().equals(labelId)))
                .map(taskMapper::map).toList();
    }

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

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.map(task);
    }

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

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
