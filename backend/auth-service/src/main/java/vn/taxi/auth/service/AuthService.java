package vn.taxi.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.auth.domain.User;
import vn.taxi.auth.event.UserCreatedEvent;
import vn.taxi.auth.event.UserEventPublisher;
import vn.taxi.auth.repository.UserRepository;
import vn.taxi.auth.security.JwtService;
import vn.taxi.auth.web.dto.AuthResponse;
import vn.taxi.auth.web.dto.LoginRequest;
import vn.taxi.auth.web.dto.RegisterRequest;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher userEventPublisher;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userEventPublisher = userEventPublisher;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.phone(),
                request.roleEnum()
        );
        user = userRepository.save(user);

        userEventPublisher.publish(new UserCreatedEvent(
                user.getId(), user.getUsername(), user.getPhone(), user.getRole()
        ));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }
}
