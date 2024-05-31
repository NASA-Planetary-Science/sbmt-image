package edu.jhuapl.sbmt.image.model;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public record CylindricalBounds(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
{
	private static final Key<Double> minLatitudeKey = Key.of("minLatitude");
	private static final Key<Double> maxLatitudeKey = Key.of("maxLatitude");
	private static final Key<Double> minLongitudeKey = Key.of("minLongitude");
	private static final Key<Double> maxLongitudeKey = Key.of("maxLongitude");
	private static final Key<CylindricalBounds> CYLINDRICAL_BOUNDS_KEY = Key.of("cylindricalBounds");

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(CYLINDRICAL_BOUNDS_KEY, (metadata) -> {

	        double minLat = metadata.get(minLatitudeKey);
	        double maxLat = metadata.get(maxLatitudeKey);
	        double minLon = metadata.get(minLongitudeKey);
	        double maxLon = metadata.get(maxLongitudeKey);
	        return new CylindricalBounds(minLat, maxLat, minLon, maxLon);

		}, CylindricalBounds.class, bounds -> {

			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
	        result.put(minLatitudeKey, bounds.minLatitude);
	        result.put(maxLatitudeKey, bounds.maxLatitude);
	        result.put(minLongitudeKey, bounds.minLongitude);
	        result.put(maxLongitudeKey, bounds.maxLongitude);
	        return result;
		});
	}
}

//public class CylindricalBounds
//{
//	private static final  Key<Double> minLatitudeKey = Key.of("minLatitude");
//	private static final  Key<Double> maxLatitudeKey = Key.of("maxLatitude");
//	private static final  Key<Double> minLongitudeKey = Key.of("minLongitude");
//	private static final  Key<Double> maxLongitudeKey = Key.of("maxLongitude");
//	private static final Key<CylindricalBounds> CYLINDRICAL_BOUNDS_KEY = Key.of("cylindricalBounds");
//
//	private double minLatitude;
//	private double maxLatitude;
//	private double minLongitude;
//	private double maxLongitude;
//
//	/**
//	 * @param minLatitude
//	 * @param maxLatitude
//	 * @param minLongitude
//	 * @param maxLongitude
//	 */
//	public CylindricalBounds(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
//	{
//		this.minLatitude = minLatitude;
//		this.maxLatitude = maxLatitude;
//		this.minLongitude = minLongitude;
//		this.maxLongitude = maxLongitude;
//	}
//
//	public double getMinLatitude()
//	{
//		return minLatitude;
//	}
//
//	public void setMinLatitude(double minLatitude)
//	{
//		this.minLatitude = minLatitude;
//	}
//
//	public double getMaxLatitude()
//	{
//		return maxLatitude;
//	}
//
//	public void setMaxLatitude(double maxLatitude)
//	{
//		this.maxLatitude = maxLatitude;
//	}
//
//	public double getMinLongitude()
//	{
//		return minLongitude;
//	}
//
//	public void setMinLongitude(double minLongitude)
//	{
//		this.minLongitude = minLongitude;
//	}
//
//	public double getMaxLongitude()
//	{
//		return maxLongitude;
//	}
//
//	public void setMaxLongitude(double maxLongitude)
//	{
//		this.maxLongitude = maxLongitude;
//	}
//
//	public static void initializeSerializationProxy()
//	{
//		InstanceGetter.defaultInstanceGetter().register(CYLINDRICAL_BOUNDS_KEY, (metadata) -> {
//
//	        double minLat = metadata.get(minLatitudeKey);
//	        double maxLat = metadata.get(maxLatitudeKey);
//	        double minLon = metadata.get(minLongitudeKey);
//	        double maxLon = metadata.get(maxLongitudeKey);
//
//	       CylindricalBounds result = new CylindricalBounds(minLat, maxLat, minLon, maxLon);
//
//			return result;
//		}, CylindricalBounds.class, bounds -> {
//			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
//	        result.put(minLatitudeKey, bounds.minLatitude);
//	        result.put(maxLatitudeKey, bounds.maxLatitude);
//	        result.put(minLongitudeKey, bounds.minLongitude);
//	        result.put(maxLongitudeKey, bounds.maxLongitude);
//	        return result;
//		});
//	}
//
//	@Override
//	public String toString()
//	{
//		return "CylindricalBounds [minLatitude=" + minLatitude + ", maxLatitude=" + maxLatitude + ", minLongitude="
//				+ minLongitude + ", maxLongitude=" + maxLongitude + "]";
//	}
//}
