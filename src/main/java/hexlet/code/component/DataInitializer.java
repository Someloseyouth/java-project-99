package hexlet.code.component;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.LabelServiceImpl;
import hexlet.code.service.TaskStatusServiceImpl;
import hexlet.code.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusServiceImpl taskStatusService;
    @Autowired
    private LabelServiceImpl labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByEmail("hexlet@example.com").isEmpty()) {
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
        var existing = taskStatusService.getAll().stream()
                .filter(s -> s.getSlug().equals(slug))
                .findFirst();

        if (existing.isEmpty()) {
            var dto = new TaskStatusCreateDTO();
            dto.setName(name);
            dto.setSlug(slug);
            taskStatusService.create(dto);
        }
    }

    private void initTaskLabel(String name) {
        var existing = labelService.getAll().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();

        if (existing.isEmpty()) {
            var dto = new LabelCreateDTO();
            dto.setName(name);
            labelService.create(dto);
        }
    }
}
