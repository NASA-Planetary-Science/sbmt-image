package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class is a bandaid to fix a time zone issue when using metadata composed of a Date, which doens't have an
 * associated time zone, but when expressed through toString or other string formatters, uses the machine's current time
 * zone to render the readable time.  This causes not only data generated (like search field stop times) to incorrectly
 * use Eastern time, but can also cause those values to be DIFFERENT if the time is displayed in another time zone.
 *
 * Until the metadata is regenerated to be UTC by default, this will convert times displayed in JSpinner views
 * into the eastern time zone for display and use in the models.
 *
 * This view should be removed once the metadata has been fixed.
 * @author steelrj1
 *
 */
public class SBMTDateSpinner extends JSpinner
{

	public SBMTDateSpinner()
	{
		setDate(new Date(1126411200000L));
		setMinimumSize(new Dimension(50, 22));
		setPreferredSize(new Dimension(250, 22));
		setMaximumSize(new Dimension(getWidth(), 22));
	}

	public SBMTDateSpinner(Date date)
	{
		this();
        SpinnerDateModel spinnerDateModel = new SpinnerDateModel(date, null, null, Calendar.DAY_OF_MONTH);
        setModel(spinnerDateModel);
	}

	public void setDate(Date date)
	{
		SpinnerDateModel spinnerDateModel = new SpinnerDateModel(date, null, null, Calendar.DAY_OF_MONTH);
        setModel(spinnerDateModel);
		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(this, "yyyy-MMM-dd HH:mm:ss.SSS");
        setEditor(dateEditor);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
//        DateTimeZone dtZone = DateTimeZone.getDefault();
        ((JSpinner.DefaultEditor) getEditor()).getTextField()
            .setFormatterFactory(new DefaultFormatterFactory(
              new DateFormatter(format)));
	}

	public Date getDate()
	{
		DateTime dt = new DateTime((Date)getValue());
//    	DateTimeZone dtZone = DateTimeZone.forID("America/New_York");
		DateTimeZone dtZone = DateTimeZone.UTC;
    	DateTime dtus = dt.withZone(dtZone);
    	return dtus.toLocalDateTime().toDateTime().toDate();
	}

	/**
	 * Returns the DateTime for the ISO compliant string in this spinner
	 * @return	the DateTime for the ISO compliant string
	 */
	public DateTime getISOFormattedTime()
	{
		Date date = getDate();
		DateTime dateTime = new DateTime(date);
		return ISODateTimeFormat.dateTimeParser().parseDateTime(dateTime.toString());
	}

	/**
	 * Returns the number of seconds between the DateTime values denoted
	 * by the <pre>start</start> and <pre>end</pre> arguments
	 * @param start	The DateTimeSpinner denoting the start time
	 * @param end	The DateTimeSpinner denoting the end time
	 * @return		The number of days between <pre>start</start> and <pre>end</pre>
	 */
	public static double getDaysBetween(SBMTDateSpinner start, SBMTDateSpinner end)
	{
        Date beginTime = (Date)start.getModel().getValue();
        Date stopTime = (Date)end.getModel().getValue();
        double total = (stopTime.getTime() - beginTime.getTime())
                / (24.0 * 60.0 * 60.0 * 1000.0);
        return total;
	}
}
