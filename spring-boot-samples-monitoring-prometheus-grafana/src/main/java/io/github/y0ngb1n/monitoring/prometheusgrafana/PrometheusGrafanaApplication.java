package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 使用 Prometheus & Grafana 监控你的 Spring Boot 应用.
 *
 * @author yangbin
 */
@SpringBootApplication
public class PrometheusGrafanaApplication {

  public static void main(String[] args) {
    SpringApplication.run(PrometheusGrafanaApplication.class, args);
  }

  @Autowired
  private CollectorRegistry collectorRegistry;

  @Bean
  public Counter requestTotalCountCollector(){
   /* Counter requestCounter = Counter.build()
      .name("io_namespace_http_requests_total").labelNames("path", "method")
      .help("Total requests.").register();*/

    return  Counter.build()
      .name("http_requests_total")
      .labelNames("path", "method", "code")
      .help("http请求总计数").register(collectorRegistry);
  }

  @Bean
  public Counter requestCounter(){
    return Counter.build()
      .name("io_namespace_http_requests_total").labelNames("path", "method")
      .help("Total requests.").register(collectorRegistry );
  }

}
