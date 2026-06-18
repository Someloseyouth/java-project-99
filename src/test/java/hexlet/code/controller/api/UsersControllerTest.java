package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UsersControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    private User testUser;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        List<UserDTO> userDTOs = om.readValue(body, new TypeReference<List<UserDTO>>() {
        });
        var expectedDTOs = userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();

        assertThat(userDTOs).isNotEmpty();
        assertThat(userDTOs)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedDTOs);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/users/" + testUser.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        UserDTO userDTO = om.readValue(body, UserDTO.class);
        var expectedDTO = userMapper.map(testUser);

        assertThat(userDTO)
                .usingRecursiveComparison()
                .isEqualTo(expectedDTO);
    }

    @Test
    public void testCreate() throws Exception {
        var data = Instancio.of(modelGenerator.getUserModel()).create();

        var request = post("/api/users").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var response = om.readValue(
                result.getResponse().getContentAsString(), UserDTO.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo(data.getEmail());
        assertThat(response.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(response.getLastName()).isEqualTo(data.getLastName());

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("password")
                .doesNotContain("passwordDigest");
    }

    @Test
    public void testUpdate() throws Exception {
        var token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var data = new HashMap<>();
        data.put("firstName", "Tony");

        var request = put("/api/users/" + testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var response = om.readValue(
                result.getResponse().getContentAsString(), UserDTO.class);

        assertThat(response.getFirstName()).isEqualTo("Tony");
        assertThat(response.getId()).isEqualTo(testUser.getId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
    }


    @Test
    public void testDelete() throws Exception {
        var token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        var request = delete("/api/users/" + testUser.getId()).with(token);

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    public void testUpdateAnotherUser() throws Exception {
        // Создаём второго пользователя и сохраняем в БД
        var anotherUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(anotherUser);

        // Токен первого пользователя
        var token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var data = new HashMap<>();
        data.put("firstName", "Alex");

        // Первый пользователь пытается обновить второго
        var request = put("/api/users/" + anotherUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isForbidden()); // Ожидаем 403
    }

    @Test
    public void testDeleteAnotherUser() throws Exception {
        var anotherUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(anotherUser);

        var token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        var request = delete("/api/users/" + anotherUser.getId())
                .with(token);

        mockMvc.perform(request).andExpect(status().isForbidden());

        // Проверяем, что второй пользователь не удалился
        assertThat(userRepository.findById(anotherUser.getId())).isPresent();
    }

    @Test
    public void testUserNotFound() throws Exception {
        var request = get("/api/users/999999").with(jwt());
        mockMvc.perform(request).andExpect(status().isNotFound());
    }
}
