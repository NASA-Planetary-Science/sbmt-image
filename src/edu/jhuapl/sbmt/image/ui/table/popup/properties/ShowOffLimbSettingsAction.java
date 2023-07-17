package edu.jhuapl.sbmt.image.ui.table.popup.properties;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.ui.offlimb.OfflimbControlsController;

import glum.gui.action.PopAction;

public class ShowOffLimbSettingsAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	private PerspectiveImageCollection<G1> aManager;
	private OfflimbControlsController offlimbController;
	private JPanel offLimbPanel;

	public ShowOffLimbSettingsAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		if (aItemL.size() != 1)
			return;

		try
		{
			offlimbController = new OfflimbControlsController(aManager, aItemL.get(0));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		GridBagConstraints gridBagConstraints = new GridBagConstraints();
//      gridBagConstraints.gridx = 0;
//      gridBagConstraints.gridy = 5;
//      gridBagConstraints.gridwidth = 2;
//      gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//      gridBagConstraints.anchor = GridBagConstraints.LINE_START;
//      gridBagConstraints.weightx = 1.0;
//		OfflimbImageControlPanel offLimbPanel = offlimbController.getControlsPanel();
//      TitledBorder titledBorder = BorderFactory.createTitledBorder("Offlimb Settings");
//      titledBorder.setTitleJustification(TitledBorder.CENTER);
      JPanel offLimbPanel = new JPanel();
//      offLimbPanel.setBorder(titledBorder);
      offLimbPanel.add(offlimbController.getControlsPanel());
      JFrame frame = new JFrame();
      frame.setTitle("Offlimb Settings");
      frame.getContentPane().add(offLimbPanel);
      frame.setAlwaysOnTop(true);
      frame.setSize(new Dimension(850, 150));
      frame.setVisible(true);
	}

}
