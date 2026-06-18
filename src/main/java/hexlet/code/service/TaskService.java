package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskFilter;
import hexlet.code.dto.TaskUpdateDTO;

import java.util.List;

public interface TaskService {
    List<TaskDTO> getAll(TaskFilter filter);

    TaskDTO create(TaskCreateDTO taskData);

    TaskDTO findById(Long id);

    TaskDTO update(TaskUpdateDTO taskData, Long id);

    void delete(Long id);

}
