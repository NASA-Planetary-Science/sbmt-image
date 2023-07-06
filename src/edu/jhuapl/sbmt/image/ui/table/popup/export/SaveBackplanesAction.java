package edu.jhuapl.sbmt.image.ui.table.popup.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class SaveBackplanesAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	/**
	 *
	 */
	private final PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public SaveBackplanesAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() == 0)
			return;

		for (G1 aItem : aItemL)
		{
			String defaultFilename = new File(aItem.getFilename() + "_DDR.IMG").getName();
			File file = CustomFileChooser.showSaveDialog(null, "Save Backplanes DDR",
					defaultFilename, "img");

			try
			{
				if (file != null)
				{
					OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

					//TODO fix
//					float[] backplanes = aItem.generateBackplanes();

//					byte[] buf = new byte[4];
//					for (int i = 0; i < backplanes.length; ++i)
//					{
//						int v = Float.floatToIntBits(backplanes[i]);
//						buf[0] = (byte) (v >>> 24);
//						buf[1] = (byte) (v >>> 16);
//						buf[2] = (byte) (v >>> 8);
//						buf[3] = (byte) (v >>> 0);
//						out.write(buf, 0, buf.length);
//					}

					out.close();
				}
			} catch (Exception ex)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}

			// Then generate the LBL file using the same filename but with a lbl
			// extension.
			// The extension is chosen to have the same case as the img file.

			try
			{
				if (file != null)
				{
//	                    String imgName = file.getName();
					File imgName = file;
					String lblName = file.getAbsolutePath();
					lblName = lblName.substring(0, lblName.length() - 4);
//	                    if (file.getAbsolutePath().endsWith("img"))
//	                        lblName += ".lbl";
//	                    else
//	                        lblName += ".LBL";

					File lblFile = new File(lblName);

					//TODO FIX
//					aItem.generateBackplanesLabel(imgName, lblFile);
				}
			} catch (Exception ex)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}

//	public void actionPerformed(ActionEvent e)
//	{
//		if (imageKeys.size() != 1)
//			return;
//		ImageKeyInterface imageKey = imageKeys.get(0);
//
//		// First generate the DDR
//
//		String defaultFilename = new File(imageKey.getName() + "_DDR.IMG").getName();
//		File file = CustomFileChooser.showSaveDialog(this.imagePopupMenu.invoker, "Save Backplanes DDR",
//				defaultFilename, "img");
//
//		try
//		{
//			if (file != null)
//			{
//				OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
//
//				this.imagePopupMenu.imageCollection.addImage(imageKey);
//				PerspectiveImage image = (PerspectiveImage) this.imagePopupMenu.imageCollection.getImage(imageKey);
//
//				this.imagePopupMenu.updateMenuItems();
//
//				float[] backplanes = image.generateBackplanes();
//
//				byte[] buf = new byte[4];
//				for (int i = 0; i < backplanes.length; ++i)
//				{
//					int v = Float.floatToIntBits(backplanes[i]);
//					buf[0] = (byte) (v >>> 24);
//					buf[1] = (byte) (v >>> 16);
//					buf[2] = (byte) (v >>> 8);
//					buf[3] = (byte) (v >>> 0);
//					out.write(buf, 0, buf.length);
//				}
//
//				out.close();
//			}
//		} catch (Exception ex)
//		{
//			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker),
//					"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
//			ex.printStackTrace();
//		}
//
//		// Then generate the LBL file using the same filename but with a lbl
//		// extension.
//		// The extension is chosen to have the same case as the img file.
//
//		try
//		{
//			if (file != null)
//			{
////                    String imgName = file.getName();
//				File imgName = file;
//				String lblName = file.getAbsolutePath();
//				lblName = lblName.substring(0, lblName.length() - 4);
////                    if (file.getAbsolutePath().endsWith("img"))
////                        lblName += ".lbl";
////                    else
////                        lblName += ".LBL";
//
//				File lblFile = new File(lblName);
//
//				this.imagePopupMenu.imageCollection.addImage(imageKey);
//				PerspectiveImage image = (PerspectiveImage) this.imagePopupMenu.imageCollection.getImage(imageKey);
//
//				this.imagePopupMenu.updateMenuItems();
//
//				image.generateBackplanesLabel(imgName, lblFile);
//			}
//		} catch (Exception ex)
//		{
//			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker),
//					"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
//			ex.printStackTrace();
//		}
//
//	}
}