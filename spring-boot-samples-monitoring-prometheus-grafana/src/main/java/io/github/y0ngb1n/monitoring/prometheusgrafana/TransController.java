package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/index")
public class TransController {


    String job_id="001";
    String date="202003201028";




  String instance_id=UUID.randomUUID().toString();

  //中文字查询接口调用总量，instance_id随机生成，不同服务instance_id不同，每次重启时会改变，避免在同一天name和labels相同导致历史数据被覆盖
  //查询时将相同name不同labels的记录汇总即为历史总调用量
  public static final Counter cnWordsCounter = Counter.build()
    .name("trans_words_cn_total_count")
    .labelNames("year", "month","day","instance")
    .help("中文字查询接口调用总量")
    .register();

  //中文字查询接口今日调用总量，每天凌晨自动重置为0，instance_id由运维人员分配，每个服务实例只有一个记录，将所有实例记录汇总即为今日调用总量(重启之后原有记录会被覆盖，导致今日数据不准确)
  //Gauge cnWordsTodayGauge = Gauge.build("trans_words_cn_today_count", "中文字查询接口今日调用总量").create();
  Gauge cnWordsTodayGauge= Gauge.build("trans_words_cn_today_count", "中文字查询接口今日调用总量").create();

  String url = "47.97.158.62:9091";
  PushGateway pg = new PushGateway(url);
  CollectorRegistry registry = new CollectorRegistry();
  Map<String, String> groupingKey = new HashMap<>();

  //系统启动时自动执行一次；每天凌晨定时任务执行一次重置
  public void reset(){
   // cnWordsTodayGauge = Gauge.build("trans_words_cn_today_count", "中文字查询接口今日调用总量").create();
    cnWordsTodayGauge.set(0);
    try {
      pg.push(registry,"assist-service_"+job_id,groupingKey);
    }catch (Exception e){
      log.error("xxxx重置失败",e);
    }
  }

  {
   // init0();
    cnWordsTodayGauge.register(registry);

    groupingKey.put("instance", "new_instance");
  //  groupingKey.put("action","trans4");
   // groupingKey.put("year","2020");

    log.info("instance_id=="+instance_id);
  }

  @GetMapping("/cn")
  public void cn(HttpServletRequest request) throws Exception{
    cnWordsCounter.labels("2020","3","20",instance_id).inc();
    cnWordsTodayGauge.inc();

    try {
      pg.push(cnWordsCounter, "assist-service_"+job_id+"_"+date);
      pg.push(registry,"assist-service_"+job_id,groupingKey);
    } catch (IOException e) {
      log.warn("",e);
    }
  }

  @GetMapping("/reset")
  public void init(HttpServletRequest request) throws Exception{
    reset();
  }




}
