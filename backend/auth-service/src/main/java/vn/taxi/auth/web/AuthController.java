package vn.taxi.auth.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.auth.domain.User;
import vn.taxi.auth.repository.UserRepository;
import vn.taxi.auth.service.AuthService;
import vn.taxi.auth.web.dto.AuthResponse;
import vn.taxi.auth.web.dto.LoginRequest;
import vn.taxi.auth.web.dto.MeResponse;
import vn.taxi.auth.web.dto.RegisterRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Gateway đã verify JWT và gắn header X-User-Id trước khi forward request tới đây.
     */
    @GetMapping("/me")
    public MeResponse me(@RequestHeader("X-User-Id") String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
        return new MeResponse(user.getId(), user.getUsername(), user.getPhone(),
                user.getRole().name(), user.getStatus().name());
    }
}
