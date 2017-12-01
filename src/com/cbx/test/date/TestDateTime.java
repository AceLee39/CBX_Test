// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.zkoss.util.Locales;

import com.cbx.ws.rest.util.CpmDateUtil;
import com.core.cbx.common.logging.LogFactory;
import com.core.cbx.common.type.DateTime;
import com.core.cbx.common.util.NumberUtil;
import com.core.cbx.data.entity.EntityConstants;

import sun.util.calendar.ZoneInfo;

/**
 * @author Ace.Li
 *
 */
public class TestDateTime {
    private static final String CBX_DATE_TIME = "YYYY-MM-DD hh:mm:ss";
    private static final String JSON_FORMAT_TIMESTAMP = "YYYY-MM-DDThh:mm:ss.ffffff";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE_UTC = "UTC";
    private static final String STR_ZONE = "0";

    /**
     * @param args
     * @throws ParseException
     */
    public static void main(final String[] args) throws ParseException {
        LogFactory.getInstance().init(null);
        final String date = "11/03/2017 16:49:17";
        final String defFormat = "MM/dd/yyyy HH:mm:ss";
        final DateTime defDateTime = DateTime.parseToDateTime(date, defFormat);

        final DateTime dateTime = domainTimeConverter(defDateTime, "UTC+8");
        final String defFormatss = "MM/DD/YYYY hh:mm:ss";
        final String text = dateTime.format(defFormatss);
        System.out.println(defDateTime);
        System.out.println(dateTime);
        System.out.println(text);
        final Calendar c = Calendar.getInstance();


        final Date now = c.getTime();
        System.out.println(c.getTime());
        System.out.println(c.getTime().getTime());
        final TimeZone timeZone = TimeZone.getTimeZone("UTC");
        timeZone.setRawOffset(3*3600*1000+30*60*1000);
        final DateTime forInstant = DateTime.forInstant(now.getTime(), timeZone);
        System.out.println(forInstant);
        System.out.println(forInstant.getMilliseconds(TimeZone.getTimeZone("UTC")));
        final String l = "UTC-01:00";
        final String minute = StringUtils.substringAfter(l, EntityConstants.SEPARATOR_COLON);
        System.out.println(minute);
        final String hour = StringUtils.substringBetween(l, "UTC", EntityConstants.SEPARATOR_COLON);
        System.out.println(hour);
        System.out.println(NumberUtil.toLong(hour));
        timeZone.setRawOffset(3*3600*1000+30*60*1000);
        System.out.println(c.getTime());
        final DateTime dd = DateTime.now(timeZone);
        System.out.println(dd.format(CBX_DATE_TIME));
        System.out.println(CpmDateUtil.toCpmStr(dd.format(CBX_DATE_TIME)));
        final String fmt = "MM/dd/yyyy HH:mm:ss";
        final DateFormat df = getDateFormat(fmt);
        System.out.println(df.format(c.getTime()));
    }

    public static DateTime domainTimeConverter(final DateTime dateTime, final String timeZone) {
        if (dateTime == null) {
            return null;
        }
        DateTime result = dateTime;
        if (StringUtils.isNotEmpty(timeZone)) {
            final String hour = getTimeZoneHour(timeZone);
            final String minute = getTimeZoneMinute(timeZone);
            if (hour.indexOf('-') > -1) {
                result = result.minus(0, 0, 0, NumberUtil.toLong(StringUtils.substringAfter(hour, "-"), 0L).intValue(),
                        NumberUtil.toLong(minute, 0L).intValue(), 0, DateTime.DayOverflow.LastDay);
            } else {
                result = result.plus(0, 0, 0, Integer.valueOf(hour),
                        NumberUtil.toLong(minute, 0L).intValue(), 0, DateTime.DayOverflow.FirstDay);
            }
        }
        return result;
    }


    private static String getTimeZoneMinute(final String timeZone) {
        final String minute = StringUtils.substringAfter(timeZone, EntityConstants.SEPARATOR_COLON);
        return StringUtils.isBlank(minute) ? STR_ZONE : minute;
    }

    private static String getTimeZoneHour(final String timeZone) {
        final String hour = StringUtils.substringBetween(timeZone, TIME_ZONE_UTC, EntityConstants.SEPARATOR_COLON);
        return StringUtils.isBlank(hour) ? STR_ZONE : hour;
    }

    protected static DateFormat getDateFormat(final String fmt) {
        final TimeZone timeZone = ZoneInfo.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat(fmt,Locales.getLocale("zh_CN"));
        df.setTimeZone(timeZone);
        return df;
    }

}
