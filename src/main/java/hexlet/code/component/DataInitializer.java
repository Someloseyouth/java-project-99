package hexlet.code.component;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusService taskStatusService;

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
}
