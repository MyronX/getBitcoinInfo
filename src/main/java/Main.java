

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    public static void main(String[] args)  {
//
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        try {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleWithFixedDelay(
                    new Request(),
                    0 * 1000,
                    30 * 1000,
                    TimeUnit.MILLISECONDS
            );
        }catch (Exception e) {
            log.error("线程出错。。。请检查！！");
        }


    }



}
