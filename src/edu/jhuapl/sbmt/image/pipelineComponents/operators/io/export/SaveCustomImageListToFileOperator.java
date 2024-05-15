package edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class SaveCustomImageListToFileOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<G1, Void>
{

	public SaveCustomImageListToFileOperator()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData() throws IOException, Exception
	{
		List<G1> images = inputs;
		File file = CustomFileChooser.showSaveDialog(null, "Select File", "imagelist.txt");
		if (file == null) return;
		String filename = file.getAbsolutePath();
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        final Key<List<G1>> userImagesKey = Key.of("UserImages");
        write(userImagesKey, images, configMetadata);
        try
        {
            Serializers.serialize("UserImages", configMetadata, new File(filename));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	private <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }



	//SIMILAR TO REGULAR FILE LIST, MAY OFFER THIS DOWN THE ROAD
//	@Override
//	public void processData() throws IOException, Exception
//	{
//		List<G1> imageList = inputs;	//this is the selected list of images to save
//		File file = CustomFileChooser.showSaveDialog(null, "Select File", "imagelist.txt");
//
//		if (file != null)
//		{
//			try
//			{
//				FileWriter fstream = new FileWriter(file);
//				BufferedWriter out = new BufferedWriter(fstream);
//
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//				String nl = System.getProperty("line.separator");
//				out.write("#Image_Name Image_Time_UTC Pointing" + nl);
//				int size = imageList.size();
//				for (G1 image : imageList)
//				{
//					String name = image.getFilename();
//					Date dt = image.getDate();
//					out.write(image + " " + sdf.format(dt) + " " + nl);
//				}
//
//				out.close();
//			}
//			catch (Exception e)
//			{
//				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//						"There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);
//
//				e.printStackTrace();
//			}
//		}
//	}

}

