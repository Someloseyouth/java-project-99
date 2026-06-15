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
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class TaskMapper {
    @Autowired
    private LabelRepository labelRepository;

    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "toLabels")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "taskLabelIds", source = "labels", qualifiedByName = "toLabelIds")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "content", source = "description")
    @Mapping(target = "createdAt",
            expression = "java(model.getCreatedAt() != null ? model.getCreatedAt().toLocalDate() : null)")
    @Mapping(target = "assigneeId",
            expression = "java(model.getAssignee() != null ? model.getAssignee().getId() : null)")
    @Mapping(target = "status",
            expression = "java(model.getTaskStatus() != null ? model.getTaskStatus().getSlug() : null)")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "toLabels")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @Named("toLabelIds")
    public Set<Long> toLabelIds(Set<Label> labels) {
        if (labels == null) {
            return new HashSet<>();
        }
        HashSet<Long> result = new HashSet<>();
        for (Label l : labels) {
            result.add(l.getId());
        }
        return result;
    }

    @Named("toLabels")
    public Set<Label> toLabels(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Label> result = labelRepository.findAllById(ids);

        Set<Long> foundIds = new HashSet<>();
        for (Label l : result) {
            foundIds.add(l.getId());
        }

        for (Long id : ids) {
            if (!foundIds.contains(id)) {
                throw new ResourceNotFoundException("Label " + id + " not found");
            }
        }

        return new HashSet<>(result);
    }
}
