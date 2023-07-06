package edu.jhuapl.sbmt.image.ui;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.status.LegacyStatusHandler;
import edu.jhuapl.sbmt.image.model.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.model.eros.nis.NISSpectrum;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.NIRS3Spectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.rendering.AdvancedSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.ui.info.SpectrumInfoPanel;
import edu.jhuapl.sbmt.spectrum.ui.info.SpectrumStatisticsInfoPanel;

public class SBMTInfoWindowManagerFactory
{
	public static <S extends BasicSpectrum> void initializeModels(ModelManager modelManager, LegacyStatusHandler aStatusHandler)
	{
		//TODO FOR NOW, commenting out images since new way deals with this differently
//		SbmtInfoWindowManager.registerInfoWindowManager(ColorImage.class, m ->
//		{
//			ColorImageCollection images = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
//            return new ColorImageInfoPanel((ColorImage)m, images, aStatusHandler);
//		});
//		SbmtInfoWindowManager.registerInfoWindowManager(Image.class, m ->
//		{
//			ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
////            if (m instanceof OsirisImage)
////                return new OsirisImageInfoPanel((Image)m, images, aStatusHandler);
//            return new ImageInfoPanel((Image)m, images, aStatusHandler);
//		});
		SbmtInfoWindowManager.registerInfoWindowManager(BasicSpectrumRenderer.class, m -> new SpectrumInfoPanel(((BasicSpectrumRenderer<S>)m).getSpectrum(), modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(AdvancedSpectrumRenderer.class, m -> new SpectrumInfoPanel(((AdvancedSpectrumRenderer<S>)m).getSpectrum(), modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(NIS.class, m -> new SpectrumInfoPanel((NISSpectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(OTES.class, m -> new SpectrumInfoPanel((OTESSpectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(OVIRS.class, m -> new SpectrumInfoPanel((OVIRSSpectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(NIRS3Spectrum.class, m -> new SpectrumInfoPanel((NIRS3Spectrum)m, modelManager));
		SbmtInfoWindowManager.registerInfoWindowManager(SpectrumStatistics.class, m -> new SpectrumStatisticsInfoPanel((SpectrumStatistics<S>)m,modelManager));
	}
}
