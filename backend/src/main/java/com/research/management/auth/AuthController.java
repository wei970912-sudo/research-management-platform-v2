package com.research.management.auth;

import com.research.management.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ApiResponse<AuthUserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            httpServletRequest.login(request.username(), request.password());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误。", ex);
        }
        return ApiResponse.ok(currentUser());
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            request.logout();
        } catch (Exception ignored) {
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ApiResponse.ok(true);
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserResponse> me() {
        return ApiResponse.ok(currentUser());
    }

    @PostMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        AppUserEntity user = currentUserEntity();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确。");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ApiResponse.ok(true);
    }

    @GetMapping("/users")
    public ApiResponse<List<UserSummaryResponse>> users() {
        List<UserSummaryResponse> users = userRepository.findAll().stream()
                .map(user -> new UserSummaryResponse(user.getUsername(), user.getDisplayName(), user.getRole()))
                .toList();
        return ApiResponse.ok(users);
    }

    @PostMapping("/users")
    public ApiResponse<UserSummaryResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsById(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在。");
        }

        AppUserEntity user = new AppUserEntity();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRole(request.role());
        userRepository.save(user);
        return ApiResponse.ok(new UserSummaryResponse(user.getUsername(), user.getDisplayName(), user.getRole()));
    }

    @PostMapping("/users/{username}/reset-password")
    public ApiResponse<Boolean> resetPassword(
            @PathVariable String username,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        AppUserEntity user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在。"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ApiResponse.ok(true);
    }

    private AuthUserResponse currentUser() {
        AppUserEntity user = currentUserEntity();
        return new AuthUserResponse(user.getUsername(), user.getDisplayName(), user.getRole());
    }

    private AppUserEntity currentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录。");
        }

        return userRepository.findById(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在。"));
    }
}
