package com.mehti.taskmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIntegrationTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ObjectMapper json;

	@Test
	void registerLoginAndManageTasks() throws Exception {
		String email = uniqueEmail();
		register(email).andExpect(status().isCreated()).andExpect(jsonPath("$.tokenType").value("Bearer"));
		String token = login(email, "Str0ngPass!");

		long id = idOf(perform(post("/api/v1/tasks"), token, """
				{"title":"Write README","priority":"HIGH","dueDate":"2030-01-15"}""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("TODO"))
				.andExpect(jsonPath("$.priority").value("HIGH")));

		perform(put("/api/v1/tasks/" + id), token, """
				{"title":"Write README","status":"DONE"}""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"));

		mvc.perform(get("/api/v1/tasks").param("status", "DONE").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].id").value(id));

		mvc.perform(delete("/api/v1/tasks/" + id).header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/v1/tasks/" + id).header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void usersCannotAccessEachOthersTasks() throws Exception {
		String alice = uniqueEmail();
		String bob = uniqueEmail();
		register(alice);
		register(bob);
		String aliceToken = login(alice, "Str0ngPass!");
		String bobToken = login(bob, "Str0ngPass!");

		long id = idOf(perform(post("/api/v1/tasks"), aliceToken, "{\"title\":\"Private\"}"));

		mvc.perform(get("/api/v1/tasks/" + id).header("Authorization", "Bearer " + bobToken))
				.andExpect(status().isNotFound());
		mvc.perform(delete("/api/v1/tasks/" + id).header("Authorization", "Bearer " + bobToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidInputAndMissingToken() throws Exception {
		mvc.perform(get("/api/v1/tasks")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer not-a-jwt")).andExpect(status().isUnauthorized());

		mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fullName\":\"\",\"email\":\"bad\",\"password\":\"123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.email").exists())
				.andExpect(jsonPath("$.errors.password").exists());

		String email = uniqueEmail();
		register(email);
		register(email).andExpect(status().isConflict());
		mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"WrongPass1\"}"))
				.andExpect(status().isUnauthorized());
	}

	private ResultActions register(String email) throws Exception {
		return mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"Str0ngPass!\"}"));
	}

	private String login(String email, String password) throws Exception {
		String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return json.readTree(body).get("accessToken").asText();
	}

	private ResultActions perform(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
		return mvc.perform(request.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private long idOf(ResultActions result) throws Exception {
		JsonNode node = json.readTree(result.andReturn().getResponse().getContentAsString());
		return node.get("id").asLong();
	}

	private static String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@test.local";
	}
}
