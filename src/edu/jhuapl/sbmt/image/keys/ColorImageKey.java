package edu.jhuapl.sbmt.image.keys;

import java.io.File;

import edu.jhuapl.sbmt.model.image.Image.ImageKey;

public class ColorImageKey
{
    public ImageKey redImageKey;
    public ImageKey greenImageKey;
    public ImageKey blueImageKey;

    public ColorImageKey(ImageKey redImage, ImageKey greenImage, ImageKey blueImage)
    {
        this.redImageKey = redImage;
        this.greenImageKey = greenImage;
        this.blueImageKey = blueImage;
    }

    @Override
    public boolean equals(Object obj)
    {
        return redImageKey.equals(((ColorImageKey)obj).redImageKey) &&
        greenImageKey.equals(((ColorImageKey)obj).greenImageKey) &&
        blueImageKey.equals(((ColorImageKey)obj).blueImageKey);
    }

    @Override
    public String toString()
    {
        // Find the start and stop indices of number part of the name. Should be
        // the same for all 3 images.
        String name = new File(redImageKey.name).getName();
        char[] buf = name.toCharArray();
        int ind0 = -1;
        int ind1 = -1;
        for (int i = 0; i<buf.length; ++i)
        {
            if (Character.isDigit(buf[i]) && ind0 == -1)
                ind0 = i;
            else if(!Character.isDigit(buf[i]) && ind0 >= 0)
            {
                ind1 = i;
                break;
            }
        }

        if (buf[ind0] == '0')
            ++ind0;

        return
        "R: " + new File(redImageKey.name).getName().substring(ind0, ind1) + ", " +
        "G: " + new File(greenImageKey.name).getName().substring(ind0, ind1) + ", " +
        "B: " + new File(blueImageKey.name).getName().substring(ind0, ind1);
    }
}

