# java_SimpleDateFormat线程不安全问题及解决方法
[CSDN相关问题](http://blog.csdn.net/suifeng3051/article/details/25226027/)
##背景
    最近在多线程环境下使用SimpleDateFormat出现一个莫名其妙的错误,输入的时间和输出的时间大相径庭,经过搜集资料发现有人遇到类似的问题,查阅java源码代码后发现
    SimpleDateFormat是非线程安全的,其注释如下：
    
     * Date formats are not synchronized.
     * It is recommended to create separate format instances for each thread.
     * If multiple threads access a format concurrently, it must be synchronized
     * externally.
    
而项目中日类控件代码片断如下:
    
    public final class DateUtil {
      private static final SimpleDateFormat YYYYMMDD_FORMAT = new SimpleDateFormat("yyyyMMdd");
      public static Date getUtilDateByShortStr(String datestr) {
              try {
                  return YYYYMMDD_FORMAT.parse(datestr);
              } catch (ParseException e) {
                  throw new SasException(".......");
              }
          }
    }

##解决方案    

解决方法一：把SimpleDateFormat作为局部变量而非全局变量
```
        public final class DateUtil {
          public static Date getUtilDateByShortStr(String datestr) {
                  try {
                      SimpleDateFormat YYYYMMDD_FORMAT = new SimpleDateFormat("yyyyMMdd");
                      return YYYYMMDD_FORMAT.parse(datestr);
                  } catch (ParseException e) {
                      throw new SasException("error.dateformate");
                  }
          }
         }
```

解决方法二：把全局变量加synchronized
```
    public final class DateUtil {
           private static final SimpleDateFormat YYYYMMDD_FORMAT = new SimpleDateFormat("yyyyMMdd");
           public static Date getUtilDateByShortStr(String datestr) {
                   try {
                       synchronized (YYYYMMDD_FORMAT) {
                            return YYYYMMDD_FORMAT.parse(datestr);
                       }
                   } catch (ParseException e) {
                       throw new SasException("error.dateformate");
                   }
               }
         } 
```

解决方法三：把SimpleDateFormat放到ThreadLocal中
```
      private static final ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
          return new SimpleDateFormat("yyyy-MM-dd HH");
        }
      };
```

解决方法四：使用线程安全的DateFormat，在Github上已经有人写了一个线程安全的，我们可以直接拿来用，请点击下面链接查看
[github](https://gist.github.com/pablomoretti/9748230/)

总结：第一种方法会创建大量的对象，第二种方法会阻塞其他线程的执行，第三种方法最优。
