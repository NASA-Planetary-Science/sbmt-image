package edu.jhuapl.sbmt.image.model;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public record BinExtents(Integer xExtent, Integer yExtent, Integer xFinalExtent, Integer yFinalExtent)
{
	private static final Key<BinExtents> IMAGE_BIN_EXTENTS_KEY = Key.of("binExtents");
	private static final Key<Integer> X_EXTENT_KEY = Key.of("xExtent");
	private static final Key<Integer> Y_EXTENT_KEY = Key.of("yExtent");
	private static final Key<Integer> X_FINAL_EXTENT_KEY = Key.of("xFinalExtent");
	private static final Key<Integer> Y_FINAL_EXTENT_KEY = Key.of("yFinalExtent");

	public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(IMAGE_BIN_EXTENTS_KEY, (source) -> {

    		Integer xExtent = source.get(X_EXTENT_KEY);
    		Integer yExtent = source.get(Y_EXTENT_KEY);
    		Integer xFinalExtent = source.get(X_FINAL_EXTENT_KEY);
    		Integer yFinalExtent = source.get(Y_FINAL_EXTENT_KEY);
    		return new BinExtents(xExtent, yExtent, xFinalExtent, yFinalExtent);

    	}, BinExtents.class, extent -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(X_EXTENT_KEY, extent.xExtent);
    		result.put(Y_EXTENT_KEY, extent.yExtent);
    		result.put(X_FINAL_EXTENT_KEY, extent.xFinalExtent);
    		result.put(Y_FINAL_EXTENT_KEY, extent.yFinalExtent);

    		return result;
    	});
	}

}