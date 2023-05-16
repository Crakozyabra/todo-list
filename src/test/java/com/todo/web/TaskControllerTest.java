package com.todo.web;

import com.todo.config.AppConfig;
import com.todo.config.WebConfig;
import com.todo.dao.TaskDAO;
import com.todo.domain.Status;
import com.todo.domain.Task;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static com.todo.web.TaskTestData.task1;

/**
 * Test connects to a running server to perform full, end-to-end HTTP tests.
 * The local server must be running before the tests!
 * In the web layer tests transaction does not rollback:
 * https://stackoverflow.com/questions/46729849/transactions-in-spring-boot-testing-not-rolled-back
 */

@ExtendWith(SpringExtension.class)
@SpringJUnitWebConfig(classes = {
        AppConfig.class, WebConfig.class
})
@Sql("classpath:db/init-populate.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskControllerTest {

    public final static String URL = "http://localhost:8080/";

    @Autowired
    TaskDAO taskDAO;

    private WebTestClient client;

    @BeforeEach
    public void setUp() {
        client = WebTestClient.bindToServer(new ReactorClientHttpConnector())
                .baseUrl(URL)
                .build();
    }

    @Test
    public void getAll() {
        client
                .get()
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody();
    }

    @Order(1)
    @Test
    public void delete() {
        client
                .post().uri("/tasks/" + task1.getId())
                .exchange()
                .expectStatus().isFound()
                .expectBody().isEmpty();
        Assertions.assertFalse(taskDAO.findById(task1.getId()).isPresent());
    }

    @Test
    public void getFormForCreate() {
        client
                .get().uri("/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody();
    }

    @Order(1)
    @Test
    public void getFormForUpdate() {
        boolean descriptionExist =
                client
                        .get().uri("/tasks?modifiedTaskId=" + task1.getId())
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                        .expectBody().returnResult().toString().contains(task1.getDescription());
        Assertions.assertTrue(descriptionExist);
    }

    @Test
    public void processFormWithErrors() {
        boolean errorsExists = client
                .post().uri("/tasks")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "taskForm")
                        .with("id", TaskTestData.task1.getId().toString())
                        .with("description", "")
                        .with("status", TaskTestData.task1.getStatus().name())
                )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody().returnResult().toString().contains("Description errors");
        Assertions.assertTrue(errorsExists);
    }

    @Test
    public void processFormWithRedirectAndUpdatedValue() {
        client
                .post().uri("/tasks")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "taskForm")
                        .with("id", task1.getId().toString())
                        .with("description", TaskTestData.updatedDescription)
                        .with("status", Status.IN_PROGRESS.name())
                )
                .exchange()
                .expectStatus().isFound()
                .expectBody().isEmpty();
        Task updated = taskDAO.findById(task1.getId()).orElse(null);
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(TaskTestData.updatedDescription, updated.getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    public void processFormWithRedirectAndSaveValue() {
        client
                .post().uri("/tasks")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "taskForm")
                        .with("description", TaskTestData.updatedDescription)
                        .with("status", Status.IN_PROGRESS.name())
                )
                .exchange()
                .expectStatus().isFound()
                .expectBody().isEmpty();
        Assertions.assertEquals(16, taskDAO.count());
    }
}
