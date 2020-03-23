package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Task implements Runnable {

  private String instance;

  private String day;

  private PushGateway pushGateway;

  Counter baiduBceTotalCounter;

  Gauge baiduBceTodayGauge;

  private CollectorRegistry registry;

  int time;

  int count ;

  private Map<String, String> groupingKey = new HashMap<>();
  private Map<String, String> todayGroupingKey = new HashMap<>();

  public Task(String instance,String day,PushGateway pushGateway,Counter baiduBceTotalCounter,Gauge baiduBceTodayGauge,int time, CollectorRegistry registry,int count){
    log.info("{} {} init ",instance,day);
    this.instance=instance;
    this.day=day;
    this.pushGateway=pushGateway;
  //  this.baiduBceTotalCounter=baiduBceTotalCounter;
  //  this.baiduBceTodayGauge=baiduBceTodayGauge;
    this.time=time;
    this.registry=registry;
    this.count=count;
  }

  @Override
  public void run() {
    log.info("{} {} execute ",instance,day);
   // baiduBceTodayGauge.register(registry);


    baiduBceTotalCounter = Counter.build()
      .name("baidu_bce_total_count")
      .help("中文字查询接口调用总量").create();
      //.register(registry);

    baiduBceTodayGauge = Gauge.build("baidu_bce_today_count", "中文字查询接口今日调用总量").create();//.register(registry);

    todayGroupingKey.put("instance", instance);
    groupingKey.put("instance", instance);

    //分组时间数据更新
    groupingKey.put("year", "2020");
    groupingKey.put("month", "3");
    groupingKey.put("day", day);
    groupingKey.put("time","202003"+day);



    for(int i=0;i<count;i++){
      baiduBceTotalCounter.inc();
      baiduBceTodayGauge.inc();
      try {
        log.info("{} {} push ",instance,day);
        pushGateway.push(baiduBceTotalCounter, "BaiduBceTotalCount", groupingKey);
        pushGateway.push(baiduBceTodayGauge, "BaiduBceTodayCount",todayGroupingKey);
      } catch (IOException e) {
        log.warn("", e);
      }

      try {
        TimeUnit.SECONDS.sleep(time);
      }catch (Exception e){
        e.printStackTrace();
      }

      log.info("[{}] add [{}]",instance,day);
    }
  }

}
