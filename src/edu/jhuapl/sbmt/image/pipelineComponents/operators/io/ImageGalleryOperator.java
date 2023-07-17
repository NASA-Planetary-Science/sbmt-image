package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.tools.ImageGalleryGenerator;
import edu.jhuapl.sbmt.tools.ImageGalleryGenerator.ImageGalleryEntry;

public class ImageGalleryOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Pair<IImagingInstrument, List<G1>>, Void>
{

	public ImageGalleryOperator()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData() throws IOException, Exception
	{
		IImagingInstrument instrument = inputs.get(0).getLeft();
		List<G1> results = inputs.get(0).getRight();
		ImageGalleryGenerator galleryGenerator = ImageGalleryGenerator.of(instrument);

        // Check if image search results are valid and nonempty
        if (results != null && galleryGenerator != null)
        {
            // Create list of gallery and preview image names based on results
            List<ImageGalleryEntry> galleryEntries = new LinkedList<ImageGalleryEntry>();
            for (G1 image : results)
            {
                ImageGalleryEntry entry = galleryGenerator.getEntry(image.getFilename());
                galleryEntries.add(entry);
            }

            // Don't bother creating a gallery if empty
            if (galleryEntries.isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                								"Unable to generate gallery.  Gallery images corresponding to search results are not registered.",
                								"Error",
                								JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create preview gallery based on search results
            String galleryURL = galleryGenerator.generateGallery(galleryEntries);

            // Show gallery preview in browser
            Desktop.getDesktop().browse(new File(galleryURL).toURI());
        }
	}
}
