
import com.justin.simpledateformat.SimpleDateFormatThreadSafe;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class SimpleDateFormatThreadSafeTest {
    private static final String DATE_FORMMAT = "yyyyMMdd";

    @Test
    public void test() throws ExecutionException, InterruptedException {

        Callable<Date> task = new Callable<Date>() {
            public Date call() throws Exception {
                return new SimpleDateFormatThreadSafe(SimpleDateFormatThreadSafeTest.DATE_FORMMAT).parse("20101022");
            }
        };
        // pool with 100 threads
        ExecutorService exec = Executors.newFixedThreadPool(100);
        List<Future<Date>> results = new ArrayList<Future<Date>>();
        // perform 100 date conversions
        for (int i = 0; i < 100; i++) {
            results.add(exec.submit(task));
        }
        exec.shutdown();
        // look at the results
        for (Future<Date> result : results) {
            System.out.println(result.get());
        }
    }
}
