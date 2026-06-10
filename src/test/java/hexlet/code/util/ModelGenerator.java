package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.stereotype.Component;

@Component
public class ModelGenerator {

    private final Faker faker = new Faker();

    public Model<User> getUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .toModel();
    }

    public Model<TaskStatus> getTaskStatusModel() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> "status_" + faker.lorem().characters(3, 10))
                .supply(Select.field(TaskStatus::getSlug), () -> "slug_" + faker.number().digits(8))
                .toModel();
    }

    public Model<Task> getTaskModel() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getLabels))
                .supply(Select.field(Task::getName), () -> faker.lorem().characters(3, 10))
                .toModel();
    }

    public Model<Label> getLabelModel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getTasks))
                .supply(Select.field(Label::getName), () -> "lbl_" + faker.lorem().characters(5, 10))
                .toModel();
    }
}
