package hexlet.code.component;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final UserService userService;

    private final TaskStatusRepository taskStatusRepository;

    private final LabelRepository labelRepository;

    private final TaskStatusService taskStatusService;

    private final LabelService labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!userService.existsByEmail("hexlet@example.com")) {
            UserCreateDTO dto = new UserCreateDTO();
            dto.setEmail("hexlet@example.com");
            dto.setPassword("qwerty");
            userService.create(dto);
        }

        initTaskStatus("Draft", "draft");
        initTaskStatus("To review", "to_review");
        initTaskStatus("To be fixed", "to_be_fixed");
        initTaskStatus("To publish", "to_publish");
        initTaskStatus("Published", "published");

        initTaskLabel("feature");
        initTaskLabel("bug");
    }


    private void initTaskStatus(String name, String slug) {
        if (!taskStatusRepository.existsBySlug(slug)) {
            var dto = new TaskStatusCreateDTO();
            dto.setName(name);
            dto.setSlug(slug);
            taskStatusService.create(dto);
        }
    }

    private void initTaskLabel(String name) {
        if (!labelRepository.existsByName(name)) {
            var dto = new LabelCreateDTO();
            dto.setName(name);
            labelService.create(dto);
        }
    }
}
