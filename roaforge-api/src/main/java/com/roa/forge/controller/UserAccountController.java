package com.roa.forge.controller;

import com.roa.forge.entity.UserAccount;
import com.roa.forge.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Accounts", description = "사용자 계정 관리 API")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @GetMapping
    @Operation(summary = "모든 사용자 조회")
    public List<UserAccount> getAll() {
        return userAccountService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 사용자 조회")
    public UserAccount getById(@PathVariable Long id) {
        return userAccountService.getUser(id);
    }

    @PostMapping
    @Operation(summary = "새 사용자 등록")
    public UserAccount create(@RequestBody UserAccount user) {
        return userAccountService.createUser(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "사용자 수정")
    public UserAccount update(@PathVariable Long id, @RequestBody UserAccount updated) {
        return userAccountService.updateUser(id, updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "사용자 삭제")
    public void delete(@PathVariable Long id) {
        userAccountService.deleteUser(id);
    }
}