package hexlet.code.controller.api;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.service.UserService;
import hexlet.code.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserUtils userUtils;

    @GetMapping("")
    public ResponseEntity<List<UserDTO>> index() {
        List<UserDTO> users = userService.getAll();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(users.size()));

        return ResponseEntity.ok().headers(headers).body(users);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
        return userService.create(userData);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@Valid @RequestBody UserUpdateDTO userData, @PathVariable Long id) {
        var currentUser = userUtils.getCurrentUser();
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return userService.update(userData, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        var currentUser = userUtils.getCurrentUser();
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        userService.delete(id);
    }
}
