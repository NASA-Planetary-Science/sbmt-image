package edu.jhuapl.sbmt.image.model;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public record BinTranslations(Integer xTranslation, Integer yTranslation)
{
	private static final Key<BinTranslations> IMAGE_BIN_TRANSLATIONS_KEY = Key.of("binTranslations");
	private static final Key<Integer> X_TRANSLATION_KEY = Key.of("xTranslation");
	private static final Key<Integer> Y_TRANSLATION_KEY = Key.of("yTranslation");

	public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(IMAGE_BIN_TRANSLATIONS_KEY, (source) -> {

    		Integer xTranslation = source.get(X_TRANSLATION_KEY);
    		Integer yTranslation = source.get(Y_TRANSLATION_KEY);
    		return new BinTranslations(xTranslation, yTranslation);

    	}, BinTranslations.class, translation -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(X_TRANSLATION_KEY, translation.xTranslation);
    		result.put(Y_TRANSLATION_KEY, translation.yTranslation);


    		return result;
    	});
	}
}