package com.mehti.taskmanager.auth;

import com.mehti.taskmanager.auth.dto.AuthResponse;
import com.mehti.taskmanager.auth.dto.LoginRequest;
import com.mehti.taskmanager.auth.dto.RegisterRequest;
import com.mehti.taskmanager.common.ConflictException;
import com.mehti.taskmanager.security.JwtService;
import com.mehti.taskmanager.user.User;
import com.mehti.taskmanager.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
					   AuthenticationManager authenticationManager, JwtService jwtService) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalize(request.email());
		if (users.existsByEmail(email)) {
			throw new ConflictException("Email is already registered");
		}
		users.save(new User(request.fullName().trim(), email, passwordEncoder.encode(request.password())));
		return issueToken(email);
	}

	public AuthResponse login(LoginRequest request) {
		String email = normalize(request.email());
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
		return issueToken(email);
	}

	private AuthResponse issueToken(String email) {
		return AuthResponse.bearer(jwtService.generateToken(email), jwtService.getExpirationSeconds());
	}

	private static String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
