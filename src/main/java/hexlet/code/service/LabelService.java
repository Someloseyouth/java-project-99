package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;

import java.util.List;

public interface LabelService {
    List<LabelDTO> getAll();

    LabelDTO create(LabelCreateDTO labelData);

    LabelDTO findById(Long id);

    LabelDTO update(LabelUpdateDTO labelData, Long id);

    void delete(Long id);
}
