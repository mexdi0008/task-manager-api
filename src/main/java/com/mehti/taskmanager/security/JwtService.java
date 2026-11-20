package com.mehti.taskmanager.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

	private static final int MIN_KEY_BYTES = 32;

	private final SecretKey key;
	private final long expirationSeconds;

	public JwtService(@Value("${app.jwt.secret}") String secret,
					  @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
		byte[] bytes = Decoders.BASE64.decode(secret);
		if (bytes.length < MIN_KEY_BYTES) {
			throw new IllegalStateException("JWT secret must be at least " + MIN_KEY_BYTES + " bytes (base64)");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(String subject) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(subject)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(key)
				.compact();
	}

	/** Returns the subject; throws {@link io.jsonwebtoken.JwtException} if the token is invalid or expired. */
	public String extractSubject(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}
}
