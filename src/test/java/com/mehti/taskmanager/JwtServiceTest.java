package com.mehti.taskmanager;

import com.mehti.taskmanager.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

	private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMDEyMzQ1Njc4OQ==";
	private static final String OTHER_SECRET = "YW5vdGhlci1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTAxMjM0NTY3ODk=";

	@Test
	void roundTripsSubject() {
		JwtService jwt = new JwtService(SECRET, 60);
		assertThat(jwt.extractSubject(jwt.generateToken("alice@test.local"))).isEqualTo("alice@test.local");
	}

	@Test
	void rejectsTokenSignedWithAnotherKey() {
		String foreign = new JwtService(OTHER_SECRET, 60).generateToken("alice@test.local");
		assertThatThrownBy(() -> new JwtService(SECRET, 60).extractSubject(foreign)).isInstanceOf(JwtException.class);
	}

	@Test
	void rejectsExpiredToken() {
		JwtService jwt = new JwtService(SECRET, -1);
		String token = jwt.generateToken("alice@test.local");
		assertThatThrownBy(() -> jwt.extractSubject(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void refusesWeakSecret() {
		assertThatThrownBy(() -> new JwtService("c2hvcnQ=", 60)).isInstanceOf(IllegalStateException.class);
	}
}
