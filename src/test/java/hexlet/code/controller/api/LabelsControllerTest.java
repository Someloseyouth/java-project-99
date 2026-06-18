package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LabelsControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    private Label testLabel;

    @Autowired
    private LabelMapper labelMapper;

    @BeforeEach
    public void setUp() {
        labelRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/labels").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        List<LabelDTO> labelDTOs = om.readValue(body, new TypeReference<>() {
        });
        var expectedDTOs = labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();

        assertThat(labelDTOs).isNotEmpty();
        assertThat(labelDTOs)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedDTOs);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/labels/" + testLabel.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        LabelDTO labelDTO = om.readValue(body, LabelDTO.class);
        var expectedDTO = labelMapper.map(testLabel);

        assertThat(labelDTO)
                .usingRecursiveComparison()
                .isEqualTo(expectedDTO);
    }

    @Test
    public void testCreate() throws Exception {
        var data = Instancio.of(modelGenerator.getLabelModel()).create();

        var request = post("/api/labels").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var response = om.readValue(
                result.getResponse().getContentAsString(), LabelDTO.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo(data.getName());
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "fix");

        var request = put("/api/labels/" + testLabel.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var response = om.readValue(
                result.getResponse().getContentAsString(), LabelDTO.class);

        assertThat(response.getName()).isEqualTo("fix");
        assertThat(response.getId()).isEqualTo(testLabel.getId());
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/labels/" + testLabel.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(labelRepository.findById(testLabel.getId())).isEmpty();
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var data = Instancio.of(modelGenerator.getLabelModel()).create();

        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }
}
