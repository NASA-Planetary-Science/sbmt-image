package edu.jhuapl.sbmt.image.ui.offlimb;
//package edu.jhuapl.sbmt.image2.ui.offlimb;
//
//import java.awt.HeadlessException;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//
//import edu.jhuapl.sbmt.image2.ui.ContrastSlider;
//import edu.jhuapl.sbmt.image2.ui.offlimb.OfflimbControlsController.AlphaSlider;
//import edu.jhuapl.sbmt.image2.ui.offlimb.OfflimbControlsController.DepthSlider;
//import edu.jhuapl.sbmt.image2.ui.offlimb.OfflimbControlsController.ShowBoundaryButton;
//import edu.jhuapl.sbmt.image2.ui.offlimb.OfflimbControlsController.SyncContrastSlidersButton;
//
//
///**
// * This frame is no longer used. Can likely be deleted, but leaving for now just in case.
// * This was moved to the Properties Panel  (ImageInfoPanel)
// * @author osheacm1
// *
// */
//public class OfflimbControlsFrame extends JFrame
//{
//    private OfflimbImageControlPanel panel;
//
//    public OfflimbControlsFrame(DepthSlider depthSlider, AlphaSlider alphaSlider, ContrastSlider contrastSlider, ShowBoundaryButton showBoundaryBtn, SyncContrastSlidersButton syncButton, JButton bndryColorBtn, JButton resetBtn) throws HeadlessException
//    {
//        panel = new OfflimbImageControlPanel(depthSlider, alphaSlider, contrastSlider, showBoundaryBtn, syncButton, bndryColorBtn, resetBtn);
//        init();
//    }
//
//    private void init()
//    {
//        add(panel);
//        setSize(400, 200);
//        setVisible(true);
//        setTitle("Offlimb Properties");
//    }
//
//    public OfflimbImageControlPanel getPanel()
//    {
//        return panel;
//    }
//}
