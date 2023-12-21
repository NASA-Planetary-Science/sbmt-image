package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.sbmt.image.controllers.preview.ImagePropertiesController;
import edu.jhuapl.sbmt.image.model.ImageProperty;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class MetadataPreview implements IPipelineSubscriber<HashMap<String, String>>
{
	private IPipelinePublisher<HashMap<String, String>> publisher;
	private String title;
	private JPanel tablePanel;
	private JPanel tablePanel2;
	private JPanel panel;

	public MetadataPreview(String title)
	{
		this.title = title;
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
	}

	@Override
	public void receive(List<HashMap<String, String>> items)
	{
//		try
//		{
			buildTableController(items);
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public void receive(HashMap<String, String> item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	public JFrame getPanel()
	{
		JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 1;
//		gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		frame.getContentPane().add(panel, gridBagConstraints);
		frame.setAlwaysOnTop(true);
		frame.setSize(new Dimension(500, 400));
		frame.setVisible(true);
		return frame;
	}

	@Override
	public void setPublisher(IPipelinePublisher<HashMap<String, String>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public MetadataPreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

	private void buildTableController(List<HashMap<String, String>> metadata)
	{
		List<ImageProperty> properties = new ArrayList<ImageProperty>();
		for (String str : metadata.get(0).keySet())
			properties.add(new ImageProperty(str, metadata.get(0).get(str)));
		ImagePropertiesController fitsHeaderPropertiesController = new ImagePropertiesController(properties);
		tablePanel = fitsHeaderPropertiesController.getView();

		List<ImageProperty> derivedProperties = new ArrayList<ImageProperty>();
		if (metadata.size() > 1)
		{
			for (String str : metadata.get(1).keySet())
				derivedProperties.add(new ImageProperty(str, metadata.get(1).get(str)));
		}
		ImagePropertiesController derivedPropertiesController = new ImagePropertiesController(derivedProperties);
		tablePanel2 = derivedPropertiesController.getView();



		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 1;
//		gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		if (properties.size() > 0)
		{
			JTabbedPane tabbedPane = new JTabbedPane();
//			tabbedPane.setPreferredSize(new Dimension(550, 350));
			tabbedPane.add("Derived Values", tablePanel2);
			tabbedPane.add("FITS Header", tablePanel);
			panel.add(tabbedPane, gridBagConstraints);
		}
		else
		{
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(Box.createVerticalStrut(20));
			JPanel midPanel = new JPanel();
			midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
			midPanel.add(Box.createHorizontalGlue());
			midPanel.add(new JLabel("No Metadata Available."));
			midPanel.add(Box.createHorizontalGlue());
			panel.add(midPanel);
			panel.add(Box.createVerticalStrut(20));
			panel.add(panel, gridBagConstraints);
		}
	}

}
