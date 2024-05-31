package edu.jhuapl.sbmt.image.model;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public class OrientationFactory
{
    private static final Orientation DefaultOrientation = new Orientation(ImageFlip.NONE, 0.0, true);

    public OrientationFactory()
    {
        super();
    }

    public Orientation of()
    {
        initializeSerializationProxy();
        return DefaultOrientation;
    }

    public Orientation of(ImageFlip flip, double rotation, boolean transpose)
    {
        initializeSerializationProxy();
        return new Orientation(flip, rotation, transpose);
    }

    public Orientation of(Metadata md)
    {
        initializeSerializationProxy();
        return InstanceGetter.defaultInstanceGetter().providesGenericObjectFromMetadata(MetadataKey).provide(md);
    }

    public Metadata toMetadata(Orientation orientation)
    {
        initializeSerializationProxy();
        return InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(Orientation.class).provide(orientation);
    }

    protected static Key<Orientation> MetadataKey = Key.of("ImageOrientation");

    public static void initializeSerializationProxy()
    {
        InstanceGetter instanceGetter = InstanceGetter.defaultInstanceGetter();
        if (!instanceGetter.isProvidableFromMetadata(MetadataKey))
        {
            instanceGetter.register(MetadataKey, source -> {
                ImageFlip flip = ImageFlip.of(source.get(Key.of("flip")));
                double rotation = source.get(Key.of("rotation"));
                boolean transpose = source.get(Key.of("transpose"));

                return new Orientation(flip, rotation, transpose);
            }, Orientation.class, orientation -> {
                SettableMetadata md = SettableMetadata.of(Version.of(1, 0));
                md.put(Key.of("flip"), orientation.getFlip().flip());
                md.put(Key.of("rotation"), orientation.getRotation());
                md.put(Key.of("transpose"), orientation.isTranspose());

                return FixedMetadata.of(md);
            });
        }
    }

}
