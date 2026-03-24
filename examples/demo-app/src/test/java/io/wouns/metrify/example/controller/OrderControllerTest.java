package io.wouns.metrify.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.wouns.metrify.example.DemoApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    classes = DemoApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration",
        "metrify.prefix=demo",
        "metrify.grafana.enabled=true"
    })
class OrderControllerTest {

  @org.springframework.beans.factory.annotation.Value("${local.server.port}")
  private int port;

  private HttpClient httpClient;

  @BeforeEach
  void setUp() {
    httpClient = HttpClient.newHttpClient();
  }

  @Test
  void placeOrderReturnsCompletedOrder() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder()
            .uri(uri("/api/orders?currency=USD&product=laptop&amount=99.99"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"product\":\"laptop\"");
    assertThat(response.body()).contains("\"currency\":\"USD\"");
    assertThat(response.body()).contains("\"status\":\"COMPLETED\"");
  }

  @Test
  void getActiveOrdersReturnsList() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder().uri(uri("/api/orders")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).startsWith("[");
  }

  @Test
  void getOrderCountsReturnsTotalAndCompleted() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder().uri(uri("/api/orders/count")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"total\"");
    assertThat(response.body()).contains("\"completed\"");
  }

  @Test
  void checkInventoryReturnsBoolean() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder().uri(uri("/api/inventory/laptop")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isIn("true", "false");
  }

  @Test
  void getInventoryReturnsStockMap() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder().uri(uri("/api/inventory")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"laptop\"");
    assertThat(response.body()).contains("\"phone\"");
  }

  @Test
  void metrifyEndpointReturnsDashboard() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder().uri(uri("/actuator/metrify")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"title\":\"Metrify Dashboard\"");
    assertThat(response.body()).contains("\"panels\"");
  }

  @Test
  void prometheusEndpointIsExposed() throws Exception {
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder().uri(uri("/actuator/prometheus")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("# HELP");
    assertThat(response.body()).contains("# TYPE");
  }

  private URI uri(String path) {
    return URI.create("http://localhost:" + port + path);
  }
}
