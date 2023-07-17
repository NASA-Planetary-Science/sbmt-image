package edu.jhuapl.sbmt.image.model;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public record BinSpacings(Double xSpacing, Double ySpacing, Double zSpacing)
{
	private static final Key<BinSpacings> IMAGE_BIN_SPACINGS_KEY = Key.of("binSpacings");
	private static final Key<Double> X_SPACING_KEY = Key.of("xSpacing");
	private static final Key<Double> Y_SPACING_KEY = Key.of("ySpacing");
	private static final Key<Double> Z_SPACING_KEY = Key.of("zSpacing");

	public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(IMAGE_BIN_SPACINGS_KEY, (source) -> {

    		Double xSpacing = source.get(X_SPACING_KEY);
    		Double ySpacing = source.get(Y_SPACING_KEY);
    		Double zSpacing = source.get(Y_SPACING_KEY);
    		return new BinSpacings(xSpacing, ySpacing, zSpacing);

    	}, BinSpacings.class, spacing -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(X_SPACING_KEY, spacing.xSpacing);
    		result.put(Y_SPACING_KEY, spacing.ySpacing);
    		result.put(Z_SPACING_KEY, spacing.zSpacing);

    		return result;
    	});
	}
}