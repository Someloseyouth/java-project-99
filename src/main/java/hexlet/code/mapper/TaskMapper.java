package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class TaskMapper {
    @Autowired
    private LabelRepository labelRepository;

    @Mapping(target = "labels", source = "labelIds")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "labelIds", expression = "java(model.getLabels().stream().map(l -> l.getId()).toList())")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "content", source = "description")
    @Mapping(target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toLocalDate() : null)")
    @Mapping(target = "assigneeId",
            expression = "java(model.getAssignee() != null ? model.getAssignee().getId() : null)")
    @Mapping(target = "status",
            expression = "java(model.getTaskStatus() != null ? model.getTaskStatus().getSlug() : null)")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    public List<Label> toLabels(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream()
                .map(id -> labelRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Label " + id + " not found")))
                .toList();
    }
}
