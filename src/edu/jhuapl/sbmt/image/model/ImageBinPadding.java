package edu.jhuapl.sbmt.image.model;

import java.util.HashMap;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public class ImageBinPadding
{
	public HashMap<Integer, BinExtents> binExtents = new HashMap<Integer, BinExtents>();
	public HashMap<Integer, BinTranslations> binTranslations = new HashMap<Integer, BinTranslations>();
	public HashMap<Integer, BinSpacings> binSpacings = new HashMap<Integer, BinSpacings>();
    private static final Key<ImageBinPadding> IMAGE_BIN_PADDING_KEY = Key.of("ImageBinPadding");
	private static final Key<HashMap<Integer, BinExtents>> BIN_EXTENTS_KEY = Key.of("binExtents");
	private static final Key<HashMap<Integer, BinTranslations>> BIN_TRANSLATION_KEY = Key.of("binTranslations");
	private static final Key<HashMap<Integer, BinSpacings>> BIN_SPACING_KEY = Key.of("binSpacings");


	 public static void initializeSerializationProxy()
		{
	    	InstanceGetter.defaultInstanceGetter().register(IMAGE_BIN_PADDING_KEY, (source) -> {

	    		HashMap<Integer, BinExtents> extents = source.get(BIN_EXTENTS_KEY);
	    		HashMap<Integer, BinTranslations> translations = source.get(BIN_TRANSLATION_KEY);
	    		HashMap<Integer, BinSpacings> spacings = source.get(BIN_SPACING_KEY);
	    		ImageBinPadding binPadding = new ImageBinPadding();
	    		binPadding.binExtents = extents;
	    		binPadding.binTranslations = translations;
	    		binPadding.binSpacings = spacings;

	    		return binPadding;

	    	}, ImageBinPadding.class, binPadding -> {

	    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
	    		result.put(BIN_EXTENTS_KEY, binPadding.binExtents);
	    		result.put(BIN_TRANSLATION_KEY, binPadding.binTranslations);
	    		result.put(BIN_SPACING_KEY, binPadding.binSpacings);

	    		return result;
	    	});
		}
}