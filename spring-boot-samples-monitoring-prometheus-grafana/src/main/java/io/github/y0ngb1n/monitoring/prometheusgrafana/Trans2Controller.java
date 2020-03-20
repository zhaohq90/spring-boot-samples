package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/index2")
public class Trans2Controller {

  private String instance = "001";
  private String url = "47.97.158.62:9091";

  private PushGateway pushGateway = new PushGateway(url);
  private CollectorRegistry registry = new CollectorRegistry();
  private Map<String, String> groupingKey = new HashMap<>();
  private Map<String, String> todayGroupingKey = new HashMap<>();

  //中文字查询接口调用总量，instance_id随机生成，不同服务instance_id不同，每次重启时会改变，避免在同一天name和labels相同导致历史数据被覆盖
  //查询时将相同name不同labels的记录汇总即为历史总调用量
  private final Counter baiduBceTotalCounter = Counter.build()
    .name("baidu_bce_total_count")
    .labelNames("year", "month", "day")
    .help("中文字查询接口调用总量")
    .register();

  //中文字查询接口今日调用总量，每天凌晨自动重置为0，instance_id由运维人员分配，每个服务实例只有一个记录，将所有实例记录汇总即为今日调用总量(重启之后原有记录会被覆盖，导致今日数据不准确)
  Gauge baiduBceTodayGauge = Gauge.build("baidu_bce_today_count", "中文字查询接口今日调用总量").create();

  //初始化指标标签值
  {
    baiduBceTodayGauge.register(registry);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    String timeStr = sdf.format(new Date());
    groupingKey.put("instance", instance);
    groupingKey.put("time", timeStr);

    todayGroupingKey.put("instance", instance);
  }

  //系统启动时自动执行一次；每天凌晨定时任务执行一次重置
  public void reset() {
    baiduBceTodayGauge.set(0);
    try {
      pushGateway.push(baiduBceTodayGauge, "BaiduBceTodayCount", todayGroupingKey);
    } catch (Exception e) {
      log.error("xxxx重置失败", e);
    }
  }

  @GetMapping("/cn")
  public void cn(HttpServletRequest request) throws Exception {
    baiduBceTotalCounter.labels("2020", "3", "20").inc();
    baiduBceTodayGauge.inc();

    try {
      pushGateway.push(baiduBceTotalCounter, "BaiduBceTotalCount", groupingKey);
      pushGateway.push(registry, "BaiduBceTodayCount",todayGroupingKey);
    } catch (IOException e) {
      log.warn("", e);
    }
  }

  @GetMapping("/reset")
  public void init(HttpServletRequest request) throws Exception {
    reset();
  }

}
