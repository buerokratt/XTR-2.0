package ee.ria.xtr_2_0.converter.utils;

import ee.ria.xtr_2_0.helper.Constants;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Converter for converting a date string to java.util.Calendar.
 * Uses date format as declared in Constants
 * @see Constants#DATE_FORMAT
 */
public class CalendarConverter implements Converter {

    private static SimpleDateFormat SDF = new SimpleDateFormat(Constants.DATE_FORMAT);

    /**
     *
     * Input string is converted to java.util.Date which is added as time to returned java.util.Calendar
     * String date information should be in a format as specified in Constants
     * @param type not actually used
     * @param value should be String with date in specific format
     * @return Calendar has it's time set to Date as specified in input value
     *
     * @see Constants#DATE_FORMAT
     */
    @Override
    public Object convert(Class type, Object value) {
        if (!(value instanceof String) || StringUtils.isEmpty(value)) {
            return null;
        }

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(SDF.parse((String) value));
            return calendar;
        }
        catch (ParseException e) {
            throw new ConversionException(e);
        }
    }
}
