package edu.jhuapl.sbmt.image.pipelineComponents.operators.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class SaveImageListOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<ImmutableSet<G1>, Void>
{
	@Override
	public void processData() throws IOException, Exception
	{
		List<G1> images = inputs.get(0).asList();
		File file = CustomFileChooser.showSaveDialog(null, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing" + nl);
                int size = images.size();
                for (int i = 0; i < size; ++i)
                {
                	String imageName = new File(images.get(i).getFilename()).getName();
                	Date imageDate = images.get(i).getDate();
                	String pointingSource = images.get(i).getPointingSourceType().toString();
                	out.write(imageName + " " + sdf.format(imageDate) + " " + pointingSource);
                	out.newLine();
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null), "There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
	}
}
