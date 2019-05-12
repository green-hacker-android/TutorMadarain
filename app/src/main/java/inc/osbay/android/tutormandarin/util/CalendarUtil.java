package inc.osbay.android.tutormandarin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarUtil {

    public static final long ONE_DAY_IN_MILLI = 1000 * 60 * 60 * 24;
    private static final long ONE_WEEK_IN_MILLI = 1000 * 60 * 60 * 24 * 7;

    public static String getFirstDateStringOfWeek(int weekNo) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return formatter.format(getFirstDateOfWeek(weekNo));
    }

    public static Date getFirstDateOfWeek(int weekNo) {
        Calendar tomorrow = GregorianCalendar.getInstance();
        tomorrow.setTimeInMillis(tomorrow.getTimeInMillis() + ONE_DAY_IN_MILLI);

        return new Date(tomorrow.getTimeInMillis() + (ONE_WEEK_IN_MILLI * (weekNo - 1)));
    }

    public static String getLastDateStringOfWeek(int weekNo) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return formatter.format(getLastDateOfWeek(weekNo));
    }

    public static Date getLastDateOfWeek(int weekNo) {
        Calendar lastDateOfWeek = GregorianCalendar.getInstance();
        lastDateOfWeek.setTimeInMillis(lastDateOfWeek.getTimeInMillis() + ONE_WEEK_IN_MILLI);

        return new Date(lastDateOfWeek.getTimeInMillis() + (ONE_WEEK_IN_MILLI * (weekNo - 1)));
    }
}
