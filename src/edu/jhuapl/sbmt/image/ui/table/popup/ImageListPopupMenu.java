package edu.jhuapl.sbmt.image.ui.table.popup;

import java.awt.Component;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.model.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.ui.table.popup.boundaryColor.BoundaryColorAction;
import edu.jhuapl.sbmt.image.ui.table.popup.export.ExportAction;
import edu.jhuapl.sbmt.image.ui.table.popup.properties.EditPointingAction;
import edu.jhuapl.sbmt.image.ui.table.popup.properties.ShowHeadersAction;
import edu.jhuapl.sbmt.image.ui.table.popup.properties.ShowImagePropertiesAction;
import edu.jhuapl.sbmt.image.ui.table.popup.properties.ShowOffLimbSettingsAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.CenterImageAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.ChangeNormalOffsetAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.ChangeOpacityAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.InterpolatePixelsAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.MapBoundaryAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.MapImageAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.RemapImageAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.ShowFrustumAction;
import edu.jhuapl.sbmt.image.ui.table.popup.rendering.SimulateLightingAction;
import edu.jhuapl.sbmt.spectrum.SbmtSpectrumWindowManager;

import glum.gui.action.PopupMenu;

public class ImageListPopupMenu<G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable> extends PopupMenu<G1>
{

	public ImageListPopupMenu(
            ModelManager modelManager,
            PerspectiveImageCollection<G1> aManager,
//            PerspectiveImageBoundaryCollection imageBoundaryCollection,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            Renderer renderer,
            Component invoker)
	{
		super(aManager);
		// TODO Auto-generated constructor stub

		MapImageAction<G1> mapAction = new MapImageAction<G1>(aManager);
		JCheckBoxMenuItem showHideCBMI = new JCheckBoxMenuItem(mapAction);
		showHideCBMI.setText("Map Image");
		installPopAction(mapAction, showHideCBMI);

		MapBoundaryAction<G1> mapBoundaryAction = new MapBoundaryAction<G1>(aManager);
		JCheckBoxMenuItem showHideBoundaryCBMI = new JCheckBoxMenuItem(mapBoundaryAction);
		showHideBoundaryCBMI.setText("Show Boundary");
		installPopAction(mapBoundaryAction, showHideBoundaryCBMI);

		SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
		ShowImagePropertiesAction<G1> showInfoAction = new ShowImagePropertiesAction<G1>(smallBodyModel, aManager);
		installPopAction(showInfoAction, "Properties...");


		ShowHeadersAction<G1> showHeadersAction = new ShowHeadersAction<G1>(smallBodyModel, aManager);
		installPopAction(showHeadersAction, "View Headers/Derived Values...");

//		if (spectrumPanelManager != null)
//		{
//			ShowSpectralPropertiesAction<G1> showSpectrumAction = new ShowSpectralPropertiesAction<G1>(aManager, spectrumPanelManager);
//			installPopAction(showSpectrumAction, "Spectrum...");
//		}



		InterpolatePixelsAction<G1> interpolatePixelsAction = new InterpolatePixelsAction<G1>(aManager);
		JCheckBoxMenuItem interpolatePixelsCBMI = new JCheckBoxMenuItem(interpolatePixelsAction);
		interpolatePixelsCBMI.setText("Interpolate Pixels");
		installPopAction(interpolatePixelsAction, interpolatePixelsCBMI);

		ShowOffLimbSettingsAction<G1> offlimbSettingsAction = new ShowOffLimbSettingsAction<G1>(aManager);
		installPopAction(offlimbSettingsAction, "Offlimb Settings...");

//		SaveBackplanesAction<PerspectiveImage> showBackplanesAction = new SaveBackplanesAction<PerspectiveImage>(aManager);
//		installPopAction(showBackplanesAction, "Generate Backplanes...");

		CenterImageAction<G1> centerImageAction = new CenterImageAction<G1>(aManager, renderer, List.of(smallBodyModel));
		installPopAction(centerImageAction, "Center in Window");

		ShowFrustumAction<G1> showFrustumAction = new ShowFrustumAction<G1>(aManager);
		JCheckBoxMenuItem showHideFrustumCBMI = new JCheckBoxMenuItem(showFrustumAction);
		showHideFrustumCBMI.setText("Show Frustum");
		installPopAction(showFrustumAction, showHideFrustumCBMI);

		JMenu exportMenu = new JMenu("Export as...");
		ExportAction<G1> exportAction = new ExportAction<>(aManager, invoker, exportMenu);
		installPopAction(exportAction, exportMenu);

//		ExportENVIImageAction<PerspectiveImage> exportENVIAction = new ExportENVIImageAction<PerspectiveImage>(aManager);
//		installPopAction(exportENVIAction, "Export ENVI Image...");
//
//		SaveImageAction<PerspectiveImage> saveImageAction = new SaveImageAction<PerspectiveImage>(aManager);
//		installPopAction(saveImageAction, "Export FITS Image...");
//
//		ExportInfofileAction<PerspectiveImage> exportInfofileAction = new ExportInfofileAction<PerspectiveImage>(aManager);
//		installPopAction(exportInfofileAction, "Export INFO File...");
//
//		ExportFitsInfoPairsAction<PerspectiveImage> exportFitsInfoPairsAction = new ExportFitsInfoPairsAction<PerspectiveImage>(aManager);
//		installPopAction(exportFitsInfoPairsAction, "Export FITS/Info File(s)...");

		ChangeNormalOffsetAction<G1> changeNormalOffsetAction = new ChangeNormalOffsetAction<G1>(aManager);
		installPopAction(changeNormalOffsetAction, "Change Normal Offset...");

		SimulateLightingAction<G1> simulateLightingAction = new SimulateLightingAction<G1>(aManager, renderer);
		JCheckBoxMenuItem simulateLightingCBMI = new JCheckBoxMenuItem(simulateLightingAction);
		simulateLightingCBMI.setText("Simulate Lighting");
		installPopAction(simulateLightingAction, simulateLightingCBMI);

		ChangeOpacityAction<G1> changeOpacityAction = new ChangeOpacityAction<G1>(aManager, renderer);
		installPopAction(changeOpacityAction, "Change Opacity...");

		EditPointingAction<G1> editPointingAction = new EditPointingAction<G1>(aManager);
		installPopAction(editPointingAction, "Adjust Image Pointing...");

		JMenu colorMenu = new JMenu("Boundary Color");
		BoundaryColorAction<G1> boundaryColorAction = new BoundaryColorAction<>(aManager, invoker, colorMenu);
		installPopAction(boundaryColorAction, colorMenu);

		RemapImageAction<G1> remapImageAction = new RemapImageAction<G1>(aManager);
		installPopAction(remapImageAction, "Rerender image");

//		SaveBackplanesAction<PerspectiveImage> showBackplanesAction = new SaveBackplanesAction<PerspectiveImage>(aManager);
//		installPopAction(showBackplanesAction, "Boundary Color");
//		colorMenu = new JMenu("Boundary Color");
//        this.add(colorMenu);
//        for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
//        {
//            JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new BoundaryColorAction(color.color()));
//            colorMenuItems.add(colorMenuItem);
//            colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
//            colorMenu.add(colorMenuItem);
//        }
//        colorMenu.addSeparator();
//        customColorMenuItem = new JMenuItem(new CustomBoundaryColorAction());
//        customColorMenuItem.setText("Custom...");
//        colorMenu.add(customColorMenuItem);
	}

}
