package io.github.y0ngb1n.monitoring.prometheusgrafana;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class TaskService {

  ExecutorService executorService=Executors.newCachedThreadPool();

  private String instance = "001";
  private String url = "47.97.158.62:9091";
  //yyyyMMddHHmm格式的时间字符串，用以区分一天之中不同时间段的记录（重启后会有多条记录）
  private  String time;

  private PushGateway pushGateway = new PushGateway(url);
  private CollectorRegistry registry = new CollectorRegistry();
  private Map<String, String> groupingKey = new HashMap<>();
  private Map<String, String> todayGroupingKey = new HashMap<>();

  //中文字查询接口调用总量，instance_id随机生成，不同服务instance_id不同，每次重启时会改变，避免在同一天name和labels相同导致历史数据被覆盖
  //查询时将相同name不同labels的记录汇总即为历史总调用量
  private final Counter baiduBceTotalCounter = Counter.build()
    .name("baidu_bce_total_count")
    .help("中文字查询接口调用总量")
    .register();

  //中文字查询接口今日调用总量，每天凌晨自动重置为0，instance_id由运维人员分配，每个服务实例只有一个记录，将所有实例记录汇总即为今日调用总量(重启之后原有记录会被覆盖，导致今日数据不准确)
  Gauge baiduBceTodayGauge = Gauge.build("baidu_bce_today_count", "中文字查询接口今日调用总量").create();

  //初始化指标标签值
  {
    baiduBceTodayGauge.register(registry);
    todayGroupingKey.put("instance", instance);
    groupingKey.put("instance", instance);

   // init();
  }

  //系统启动时自动执行一次；每天凌晨定时任务执行一次重置
  public void init() {
    //今日调用次数重置为0
    baiduBceTodayGauge.set(0);

    //时间字符串更新
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    time = sdf.format(now.getTime());

    //分组时间数据更新
    groupingKey.put("year", now.get(Calendar.YEAR)+"");
    groupingKey.put("month", (now.get(Calendar.MONTH) + 1+""));
    groupingKey.put("day", now.get(Calendar.DAY_OF_MONTH)+"");
    groupingKey.put("time",time);
    try {
      pushGateway.push(baiduBceTodayGauge, "BaiduBceTodayCount", todayGroupingKey);
    } catch (Exception e) {
      log.error("xxxx重置失败", e);
    }
  }

  public void add(){

    executorService.submit(new Task("001","23",pushGateway,baiduBceTotalCounter,baiduBceTodayGauge,5,registry,11));
    executorService.submit(new Task("001","24",pushGateway,baiduBceTotalCounter,baiduBceTodayGauge,5,registry,11));


    executorService.submit(new Task("002","23",pushGateway,baiduBceTotalCounter,baiduBceTodayGauge,6,registry,12));
    executorService.submit(new Task("002","24",pushGateway,baiduBceTotalCounter,baiduBceTodayGauge,6,registry,12));

    executorService.submit(new Task("003","23",pushGateway,baiduBceTotalCounter,baiduBceTodayGauge,7,registry,13));
    executorService.submit(new Task("003","24",pushGateway,baiduBceTotalCounter,baiduBceTodayGauge,7,registry,13));

  }

}
