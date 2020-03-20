package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

  public static final Counter counterDemo = Counter.build()
    .name("push_way_counter33")
    .labelNames("year", "month")
    .help("user-service异常统计")
    .register();

  Gauge guage = Gauge.build("new_metric43", "This is my custom metric.").create();

 // @Autowired
  Counter counter;

  @GetMapping
  public void index(HttpServletRequest request) throws Exception{
    String url = "47.97.158.62:9091";
    CollectorRegistry registry = new CollectorRegistry();

    log.info(guage.get()+"  333");
   // guage.inc();
    guage.register(registry);
    PushGateway pg = new PushGateway(url);
    Map<String, String> groupingKey = new HashMap<String, String>();
    groupingKey.put("instance", "new_instance");
    groupingKey.put("action","trans4");
    groupingKey.put("year","2020");

    guage .inc();

   // pg.push( "my_job", groupingKey);
   // pg.push(guage,"aaa-service");

    pg.push(registry,"bbb-service",groupingKey);
    log.info("aa");
    requestCounter.labels(request.getRequestURI(), request.getMethod()).inc();
  //  counter.labels(request.getRequestURI(), request.getMethod(),200+"").inc();

    counterDemo.labels(2020+"", "3").inc();
    try {
      pg.push(counterDemo, "ex-user-service");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @GetMapping("/view")
  public void index2(HttpServletRequest request) {
    log.info("aa2");
    requestCounter.labels(request.getRequestURI(), request.getMethod()).inc();
    //  counter.labels(request.getRequestURI(), request.getMethod(),200+"").inc();
  }


}
