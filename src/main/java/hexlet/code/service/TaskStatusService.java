package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;

import java.util.List;

public interface TaskStatusService {
    List<TaskStatusDTO> getAll();

    TaskStatusDTO create(TaskStatusCreateDTO taskStatusData);

    TaskStatusDTO findById(Long id);

    TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id);

    void delete(Long id);
}
