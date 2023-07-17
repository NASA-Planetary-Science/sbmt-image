package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageRenderingState;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class LoadCustomImageListFromFileOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
			extends BasePipelineOperator<File, Pair<List<G1>, HashMap<G1, PerspectiveImageRenderingState<G1>>>>
{

	public LoadCustomImageListFromFileOperator()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processData() throws IOException, Exception
	{
		File file = inputs.get(0);
		String filename = file.getAbsolutePath();
        if (!new File(filename).exists()) return;
		FixedMetadata metadata;
        try
        {
        	final Key<List<G1>> userImagesKey = Key.of("UserImages");
            metadata = Serializers.deserialize(new File(filename), "UserImages");
            List<G1> userImages = read(userImagesKey, metadata);
            HashMap<G1, PerspectiveImageRenderingState<G1>> renderingStates = new HashMap<G1, PerspectiveImageRenderingState<G1>>();
            for (G1 image : userImages)
            {
            	PerspectiveImageRenderingState<G1> state = new PerspectiveImageRenderingState<G1>();
            	state.isMapped = image.isMapped();
            	if (image.isMapped()) image.setStatus("Loaded");
            	state.isFrustumShowing = image.isFrustumShowing();
            	state.isBoundaryShowing = image.isBoundaryShowing();
            	state.isOfflimbShowing = image.isOfflimbShowing();
        		renderingStates.put(image,state);
        		//TODO leaving this here for now, in case I want to implement something like Nobes does for dtms
//        		updateImage(image);
            }
    		outputs.add(Pair.of(userImages, renderingStates));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

	}

    private <T> T read(Key<T> key, Metadata configMetadata)
    {
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }

	//SIMILAR TO NORMAL FILELIST - MAY OFFER IN FUTURE
//	@Override
//	public void processData() throws IOException, Exception
//	{
//		File file = CustomFileChooser.showOpenDialog(null, "Select File");
//		List<G1> images = Lists.newArrayList();
//        if (file != null)
//        {
//            try
//            {
//            	List<List<String>> fixedList = null;
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                List<String> namesOnly = new ArrayList<String>();
//                List<List<String>> results = new ArrayList<List<String>>();
//                List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
//                System.out.println("ImageResultsTableController: loadImageListButtonActionPerformed: lines size " + lines.size());
//
//                for (int i = 0; i < lines.size(); ++i)
//                {
//                    if (lines.get(i).startsWith("#"))
//                        continue;
//                    String[] words = lines.get(i).trim().split("\\s+");
//                    String imageName = words[0];
//                    System.out.println("LoadImageFileListToImageListOperator: processData: image name " + imageName);
//                    Date dt = sdf.parse(words[1]);
//                    System.out.println("LoadImageFileListToImageListOperator: processData: dt " + dt);
//                    ImageSource imageSource = ImageSource.valueFor(words[2].replace("_", " "));
//                    System.out.println("LoadImageFileListToImageListOperator: processData: image source " + imageSource);
////                    if (fixedList == null) { fixedList = imageSearchModel.getFixedList(imageSource); System.out.println("ImageResultsTableController: loadImageListButtonActionPerformed: first entry " + fixedList.get(fixedList.size()-1).get(0));}
//
////                    List<String> result = new ArrayList<String>();
////                    String name = instrument.searchQuery.getDataPath() + "/" + words[0];
////                    result.add(name);
////                    Date dt = sdf.parse(words[1]);
////                    result.add(String.valueOf(dt.getTime()));
////                    results.add(result);
////                    imageSearchModel.setImageSourceOfLastQuery(imageSource);
//                }
//
//                for (List<String> fixedEntry : fixedList)
//                {
//                    namesOnly.add(fixedEntry.get(0).substring(fixedEntry.get(0).lastIndexOf("/")+1));
//                }
////                fixedList.retainAll(results);
//                results.removeIf(entry -> {
//
//                	return !namesOnly.contains(entry.get(0).substring(entry.get(0).lastIndexOf("/")+1));
//                });
//
//                if (!((lines.size()-1) == results.size()))
//                {
//                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//                    							"Only some of the images in the file had pointing for this model;\n images without pointing (count: " + (lines.size() - results.size() - 1)  + ") have been ignored",
//                    							"Warning",
//                    							JOptionPane.ERROR_MESSAGE);
//                }
//
//
////                //TODO needed?
////                imageSearchModel.setImageResults(new ArrayList<List<String>>());
////                setImageResults(imageSearchModel.processResults(results));
////                outputs.add(getOutput())
//            }
//            catch (Exception e)
//            {
//                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//                							"There was an error reading the file.",
//                							"Error",
//                							JOptionPane.ERROR_MESSAGE);
//
//                e.printStackTrace();
//            }
//        }
//	}

}


/****
 if (userImages.size() != 0) return;
		String instrumentName = imagingInstrument == null ? "" : imagingInstrument.getType().toString();
		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + instrumentName + ".txt";
        if (!new File(filename).exists()) return;
		FixedMetadata metadata;
        try
        {
        	final Key<List<G1>> userImagesKey = Key.of("UserImages");
            metadata = Serializers.deserialize(new File(filename), "UserImages");
            userImages = read(userImagesKey, metadata);
            for (G1 image : userImages)
            {
            	PerspectiveImageRenderingState state = new PerspectiveImageRenderingState();
            	state.isMapped = image.isMapped();
            	if (image.isMapped()) image.setStatus("Loaded");
            	state.isFrustumShowing = image.isFrustumShowing();
            	state.isBoundaryShowing = image.isBoundaryShowing();
            	state.isOfflimbShowing = image.isOfflimbShowing();
        		renderingStates.put(image,state);
        		//TODO leaving this here for now, in case I want to implement something like Nobes does for dtms
//        		updateImage(image);
            }
    		setAllItems(userImages);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 */
