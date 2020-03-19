package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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

  public static final Counter counterDemo = Counter.build()
    .name("push_way_counter")
    .labelNames("code", "instance")
    .help("user-service异常统计")
    .register();



  @GetMapping
  public void index(HttpServletRequest request) {
    //统计异常
    PushGateway prometheusPush = new PushGateway("47.97.158.62:9091");
    //指标值增加
    counterDemo.labels(222+"", "botaa").inc();
    try {
      prometheusPush.push(counterDemo, "ex-user-service");
    } catch (IOException e) {
      e.printStackTrace();
    }


    log.info("aa");
    requestCounter.labels(request.getRequestURI(), request.getMethod()).inc();
  //  counter.labels(request.getRequestURI(), request.getMethod(),200+"").inc();
  }

  @GetMapping("/view")
  public void index2(HttpServletRequest request) {
    log.info("aa2");
    requestCounter.labels(request.getRequestURI(), request.getMethod()).inc();
    //  counter.labels(request.getRequestURI(), request.getMethod(),200+"").inc();
  }


}
