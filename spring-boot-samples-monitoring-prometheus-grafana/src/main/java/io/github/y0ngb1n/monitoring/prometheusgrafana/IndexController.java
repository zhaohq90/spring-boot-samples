package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/index")
public class IndexController {

  @Autowired
  private CollectorRegistry collectorRegistry;
    /*final Counter requestCounter = Counter.build()
    .name("io_namespace_http_requests_total").labelNames("path", "method")
    .help("Total requests.").register(collectorRegistry);*/

    @Autowired
  private Counter requestCounter;




 // @Autowired
  Counter counter;

  @GetMapping
  public void index(HttpServletRequest request) {
    log.info("aa");
    requestCounter.labels(request.getRequestURI(), request.getMethod()).inc();
  //  counter.labels(request.getRequestURI(), request.getMethod(),200+"").inc();
  }

}
