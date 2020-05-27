package CommonTools;

import java.time.format.DateTimeFormatter;

public interface DateTimeFormatters {
     DateTimeFormatter ddMMYYYYmitPunkt= DateTimeFormatter.ofPattern("dd.MM.yyyy");
     DateTimeFormatter yyyyMMddmitBindestrich = DateTimeFormatter.ofPattern("yyyy-MM-dd");

}
