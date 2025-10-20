package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RoleRequested;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminClientController {

    private final ClientService clientService;

    // пользователи

    @GetMapping("/users/{userId}")
    public AllUserResponse getUser(@PathVariable Long userId) {
        return clientService.findAllUser(userId);
    }

    // todo бляяяяяяяя, я заебался, босс, я устал.. еще дохуя делать, а времени нихуя :( ff
    @GetMapping("/users")
    public Page<AllUserResponse> getAllUsers(
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "roleName", required = false) String roleName,
            @PageableDefault(size = 20, sort = "banned") Pageable pageable
    ) {
        var filter = new FilterUserRequest(
                banned,
                roleName
        );
        return clientService.filterUsers(filter, pageable);
    }


// баны + роли, мб еще удаление сделать? хотя, это тоже самое, что и бан пока
    // todo мб, удаление тоже?
    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long userId) {
        var client = clientService.banUser(userId);
        return ResponseEntity.ok().body(client);
    }

    @PatchMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        var client = clientService.unbanUser(userId);
        return ResponseEntity.ok().body(client);
    }

    @PatchMapping("/users/{userId}/updateRole")
    public ResponseEntity<?> updateRole(@PathVariable Long userId,
                                        RoleRequested roleRequested) {
        var client = clientService.updateRole(userId, roleRequested.RoleName());
        return ResponseEntity.ok().body(client);
    }


}