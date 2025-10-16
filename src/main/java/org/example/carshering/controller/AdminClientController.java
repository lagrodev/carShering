package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RoleRequested;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminClientController {

    private final ClientService clientService;

    // пользователи
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return clientService.getAllUsers();
    }


    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return clientService.findUser(userId);
    }

    // todo бляяяяяяяя, я заебался, босс, я устал.. еще дохуя делать, а времени нихуя :( ff
    @PostMapping("/users/filter")
    public List<UserResponse> filterUsers(@RequestBody FilterUserRequest request) {
        return clientService.filterUsers(
                request.banned(),
                request.roleName(),
                request.sortBy(),
                request.sortOrder()
        );
    }



// баны + роли, мб еще удаление сделать? хотя, это тоже самое, что и бан пока

    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long userId) {
        clientService.banUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        clientService.unbanUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/updateRole")
    public ResponseEntity<?> updateRole(@PathVariable Long userId,
                                        RoleRequested roleRequested) {
        clientService.updateRole(userId, roleRequested.RoleName());
        return ResponseEntity.noContent().build();
    }


}