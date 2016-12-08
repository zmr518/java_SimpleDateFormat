/*
* Copyright (C), 2016-2017, 苏宁易购电子商务有限公司
* FileName: DateUtilTest.java
* Author:   16050071
* Date:     2016/12/8  10:45     
* History: 
* <author> <time> <version> <desc>
* 修改人姓名             修改时间            版本号                  描述
*/

import com.justin.simpledateformat.SimpleDateFormatThreadSafe;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author 16050071
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */

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