# java_SimpleDateFormat线程不安全问题及解决方法
[Web Site](http://blog.csdn.net/suifeng3051/article/details/25226027/)
Java SimpleDateFormat 是线程不安全的，当在多线程环境下使用一个DateFormat的时候是有问题的，如下面的例子：


    package com.heaven.threadpool;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;
    import java.util.concurrent.Callable;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.concurrent.Future;

    public class DateFormatThreadSafe {
      public static void main(String[] args) throws Exception {

        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        Callable<Date> task = new Callable<Date>() {
          public Date call() throws Exception {
            return format.parse("20101022");
          }
        };
        // pool with 5 threads
        ExecutorService exec = Executors.newFixedThreadPool(5);
        List<Future<Date>> results = new ArrayList<Future<Date>>();
        // perform 10 date conversions
        for (int i = 0; i < 10; i++) {
          results.add(exec.submit(task));
        }
        exec.shutdown();
        // look at the results
        for (Future<Date> result : results) {
          System.out.println(result.get());
        }
      }

    }
上面代码可能会引起如下问题：
问题1：

    Fri Oct 22 00:00:00 CST 2010
    Fri Oct 22 00:00:00 CST 2010
    Fri Oct 22 00:00:00 CST 2010
    Mon Nov 22 00:00:00 CST 2010
    Exception in thread "main" java.util.concurrent.ExecutionException: java.lang.NumberFormatException: For input string: ""
      at java.util.concurrent.FutureTask$Sync.innerGet(FutureTask.java:222)
      at java.util.concurrent.FutureTask.get(FutureTask.java:83)
      at com.heaven.threadpool.DateFormatThreadSafe.main(DateFormatThreadSafe.java:49)
    Caused by: java.lang.NumberFormatException: For input string: ""
      at java.lang.NumberFormatException.forInputString(NumberFormatException.java:48)
      at java.lang.Long.parseLong(Long.java:424)
      at java.lang.Long.parseLong(Long.java:461)
      at java.text.DigitList.getLong(DigitList.java:177)
      at java.text.DecimalFormat.parse(DecimalFormat.java:1298)
      at java.text.SimpleDateFormat.subParse(SimpleDateFormat.java:1589)
      at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1312)
      at java.text.DateFormat.parse(DateFormat.java:335)
      at com.heaven.threadpool.DateFormatThreadSafe$1.call(DateFormatThreadSafe.java:31)
      at com.heaven.threadpool.DateFormatThreadSafe$1.call(DateFormatThreadSafe.java:1)
      at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
      at java.util.concurrent.FutureTask.run(FutureTask.java:138)
      at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
      at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
      at java.lang.Thread.run(Thread.java:619)

问题二：

    Fri Oct 22 00:00:00 CST 2010
    Fri Oct 22 00:00:00 CST 2010
    Fri Oct 22 00:00:00 CST 2010
    Exception in thread "main" java.util.concurrent.ExecutionException: java.lang.ArrayIndexOutOfBoundsException: -1
      at java.util.concurrent.FutureTask$Sync.innerGet(FutureTask.java:222)
      at java.util.concurrent.FutureTask.get(FutureTask.java:83)
      at com.heaven.threadpool.DateFormatThreadSafe.main(DateFormatThreadSafe.java:32)
    Caused by: java.lang.ArrayIndexOutOfBoundsException: -1
      at java.text.DigitList.fitsIntoLong(DigitList.java:212)
      at java.text.DecimalFormat.parse(DecimalFormat.java:1296)
      at java.text.SimpleDateFormat.subParse(SimpleDateFormat.java:1589)
      at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1312)
      at java.text.DateFormat.parse(DateFormat.java:335)
      at com.heaven.threadpool.DateFormatThreadSafe$1.call(DateFormatThreadSafe.java:19)
      at com.heaven.threadpool.DateFormatThreadSafe$1.call(DateFormatThreadSafe.java:1)
      at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
      at java.util.concurrent.FutureTask.run(FutureTask.java:138)
      at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
      
问题三：

    Sat Oct 01 00:00:00 CST 1
    Sat Oct 01 00:00:00 CST 1
    Sun Oct 30 00:00:00 CST 2016
    Sun Oct 30 00:00:00 CST 2016
    Exception in thread "main" java.util.concurrent.ExecutionException: java.lang.NumberFormatException: For input string: "E.122E22"
      at java.util.concurrent.FutureTask$Sync.innerGet(FutureTask.java:222)
      at java.util.concurrent.FutureTask.get(FutureTask.java:83)
      at com.heaven.threadpool.DateFormatThreadSafe.main(DateFormatThreadSafe.java:32)
    Caused by: java.lang.NumberFormatException: For input string: "E.122E22"
      at sun.misc.FloatingDecimal.readJavaFormatString(FloatingDecimal.java:1224)
      at java.lang.Double.parseDouble(Double.java:510)
      at java.text.DigitList.getDouble(DigitList.java:151)
      at java.text.DecimalFormat.parse(DecimalFormat.java:1303)
      at java.text.SimpleDateFormat.subParse(SimpleDateFormat.java:1936)
      at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1312)
      at java.text.DateFormat.parse(DateFormat.java:335)
      at com.heaven.threadpool.DateFormatThreadSafe$1.call(DateFormatThreadSafe.java:19)
      at com.heaven.threadpool.DateFormatThreadSafe$1.call(DateFormatThreadSafe.java:1)
      at java.u

解决方法一：把DateFormat 放到ThreadLocal中

    package com.heaven.threadpool;

    import java.text.DateFormat;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Date;

    public class DateFormatTest {
      private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
          return new SimpleDateFormat("yyyy-MM-dd HH");
        }
      };

      public Date convert(String source) throws ParseException {
        Date d = df.get().parse(source);
        return d;
      }
    }
解决方法二：使用线程安全的DateFormat，在Github上已经有人写了一个线程安全的，我们可以直接拿来用，请点击下面链接查看
[github](https://gist.github.com/pablomoretti/9748230/)
