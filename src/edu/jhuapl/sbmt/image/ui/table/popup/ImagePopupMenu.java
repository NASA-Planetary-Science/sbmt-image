package edu.jhuapl.sbmt.image.ui.table.popup;
//package edu.jhuapl.sbmt.image2.ui.table.popup;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.event.MouseEvent;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//
//import javax.swing.JCheckBoxMenuItem;
//import javax.swing.JMenu;
//import javax.swing.JMenuItem;
//
//import vtk.vtkActor;
//import vtk.vtkProp;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.popup.PopupMenu;
//import edu.jhuapl.saavtk.util.ColorUtil;
//import edu.jhuapl.saavtk.view.light.LightCfg;
//import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
//import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
//import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
//import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
//import edu.jhuapl.sbmt.model.image.Image;
//import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
//import edu.jhuapl.sbmt.model.image.ImageSource;
//import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundary;
//import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
//
//
//public class ImagePopupMenu extends PopupMenu
//{
//    Component invoker;
//    PerspectiveImageCollection imageCollection;
//    PerspectiveImageBoundaryCollection imageBoundaryCollection;
//    private List<PerspectiveImage> selectedImages = new ArrayList<PerspectiveImage>();
//    //private List<ImageKey> keySet = new ArrayList<Image.ImageKey>();
//    JMenuItem mapImageMenuItem;
//    JMenuItem mapBoundaryMenuItem;
//    private JMenuItem showImageInfoMenuItem;
//    private JMenuItem showImageSpectrumMenuItem;
//    private JMenuItem saveToDiskMenuItem;
//    private JMenuItem saveBackplanesMenuItem;
//    private JMenuItem centerImageMenuItem;
//    JMenuItem showFrustumMenuItem;
//    private JMenuItem exportENVIImageMenuItem;
//    private JMenuItem exportInfofileMenuItem;
//    private JMenuItem exportFitsInfoPairsMenuItem;
//    private JMenuItem changeNormalOffsetMenuItem;
//    JCheckBoxMenuItem simulateLightingMenuItem;
//    private JMenuItem changeOpacityMenuItem;
//    JMenuItem hideImageMenuItem;
//    private JMenu colorMenu;
//    private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
//    private JMenuItem customColorMenuItem;
//    SbmtInfoWindowManager infoPanelManager;
//    SbmtSpectrumWindowManager spectrumPanelManager;
//    Renderer renderer;
//    ModelManager modelManager;
//    LightCfg origLightCfg;
//
//    /**
//     *
//     * @param modelManager
//     * @param type the type of popup. 0 for right clicks on items in the search list,
//     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
//     * mapped to Eros.
//     */
//    public ImagePopupMenu(
//            ModelManager modelManager,
//            PerspectiveImageCollection imageCollection,
//            PerspectiveImageBoundaryCollection imageBoundaryCollection,
//            SbmtInfoWindowManager infoPanelManager,
//            SbmtSpectrumWindowManager spectrumPanelManager,
//            Renderer renderer,
//            Component invoker)
//    {
//        this.modelManager = modelManager;
//        this.imageCollection = imageCollection;
//        this.imageBoundaryCollection = imageBoundaryCollection;
//        this.infoPanelManager = infoPanelManager;
//        this.spectrumPanelManager = spectrumPanelManager;
//        this.renderer = renderer;
//        this.invoker = invoker;
//
//        mapImageMenuItem = new JCheckBoxMenuItem(new MapImageAction(this));
//        mapImageMenuItem.setText("Map Image");
//        this.add(mapImageMenuItem);
//
//        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction(this));
//        mapBoundaryMenuItem.setText("Map Image Boundary");
//        this.add(mapBoundaryMenuItem);
//
//        if (this.infoPanelManager != null)
//        {
//            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction(this));
//            showImageInfoMenuItem.setText("Properties...");
//            this.add(showImageInfoMenuItem);
//        }
//
//        if (this.spectrumPanelManager != null)
//        {
//            showImageSpectrumMenuItem = new JMenuItem(new ShowSpectrumAction(this));
//            if (spectrumPanelManager.getNumberSpectrumModels() > 0)
//            {
//                showImageSpectrumMenuItem.setText("Spectrum...");
//                this.add(showImageSpectrumMenuItem);
//            }
//        }
//
//
//
//        saveBackplanesMenuItem = new JMenuItem(new SaveBackplanesAction(this));
//        saveBackplanesMenuItem.setText("Generate Backplanes...");
//        this.add(saveBackplanesMenuItem);
//
//        if (renderer != null)
//        {
//            centerImageMenuItem = new JMenuItem(new CenterImageAction(this));
//            centerImageMenuItem.setText("Center in Window");
//            this.add(centerImageMenuItem);
//        }
//
//        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction(this));
//        showFrustumMenuItem.setText("Show Frustum");
//        this.add(showFrustumMenuItem);
//
//        exportENVIImageMenuItem = new JMenuItem(new ExportENVIImageAction(this)); // twupy1
//        exportENVIImageMenuItem.setText("Export ENVI Image...");
//        this.add(exportENVIImageMenuItem);
//
//        saveToDiskMenuItem = new JMenuItem(new SaveImageAction(this));
//        saveToDiskMenuItem.setText("Export FITS Image...");
//        this.add(saveToDiskMenuItem);
//
//        exportInfofileMenuItem = new JMenuItem(new ExportInfofileAction(this));
//        exportInfofileMenuItem.setText("Export INFO File...");
//        this.add(exportInfofileMenuItem);
//
//        exportFitsInfoPairsMenuItem = new JMenuItem(new ExportFitsInfoPairsAction(this));
//        exportFitsInfoPairsMenuItem.setText("Export FITS/Info File(s)...");
//        this.add(exportFitsInfoPairsMenuItem);
//
//        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction(this));
//        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
//        this.add(changeNormalOffsetMenuItem);
//
//        simulateLightingMenuItem = new JCheckBoxMenuItem(new SimulateLightingAction(this));
//        simulateLightingMenuItem.setText("Simulate Lighting");
//        this.add(simulateLightingMenuItem);
//
//        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction(this));
//        changeOpacityMenuItem.setText("Change Opacity...");
//        this.add(changeOpacityMenuItem);
//
//        hideImageMenuItem = new JCheckBoxMenuItem(new HideImageAction(this));
//        hideImageMenuItem.setText("Hide Image");
//        this.add(hideImageMenuItem);
//
//        colorMenu = new JMenu("Boundary Color");
//        this.add(colorMenu);
//        for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
//        {
//            JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new BoundaryColorAction(this, color.color()));
//            colorMenuItems.add(colorMenuItem);
//            colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
//            colorMenu.add(colorMenuItem);
//        }
//        colorMenu.addSeparator();
//        customColorMenuItem = new JMenuItem(new CustomBoundaryColorAction(this));
//        customColorMenuItem.setText("Custom...");
//        colorMenu.add(customColorMenuItem);
//
//    }
//
//    public void setCurrentImage(ImageKeyInterface key)
//    {
//        imageKeys.clear();
//        imageKeys.add(key);
//
//        updateMenuItems();
//    }
//
//    public void setCurrentImages(List<ImageKeyInterface> keys)
//    {
//        imageKeys.clear();
//        imageKeys.addAll(keys);
//
//        updateMenuItems();
//    }
//
//    void updateMenuItems()
//    {
//        boolean selectMapImage = true;
//        boolean enableMapImage = true;
//        boolean selectMapBoundary = true;
//        boolean enableMapBoundary = true;
//        boolean enableCenterImage = false;
//        boolean enableShowImageInfo = false;
//        boolean enableSaveBackplanes = false;
//        boolean enableSaveToDisk = false;
//        boolean enableBulkSaveToDisk = false;
//        boolean enableChangeNormalOffset = false;
//        boolean selectShowFrustum = true;
//        boolean enableShowFrustum = true;
//        boolean enableSimulateLighting = false;
//        boolean simulateLightingOn = false;
//        boolean enableChangeOpacity = false;
//        boolean selectHideImage = true;
//        boolean enableHideImage = true;
//        boolean enableBoundaryColor = true;
//
//        for (ImageKeyInterface imageKey : imageKeys)
//        {
//            boolean containsImage = imageCollection.containsImage(imageKey);
//            boolean containsBoundary = false;
//            if (imageBoundaryCollection != null)
//                containsBoundary = imageBoundaryCollection.containsBoundary(imageKey);
//
//            if (!containsBoundary)
//            {
//                selectMapBoundary = containsBoundary;
//                enableBoundaryColor = false;
//            }
//
//            if (!containsImage)
//                selectMapImage = containsImage;
//
//            if (centerImageMenuItem != null && imageKeys.size() == 1)
//            {
//                enableCenterImage = containsBoundary || containsImage;
//            }
//
//            if (showImageInfoMenuItem != null && imageKeys.size() == 1)
//                enableShowImageInfo = containsImage;
//
//            if (imageKeys.size() == 1)
//            {
//                enableSaveBackplanes = containsImage;
//                enableSaveToDisk = containsImage;
//                enableChangeNormalOffset = containsImage;
//                enableChangeOpacity = containsImage;
//            }
//
//            if (imageKeys.size() >= 1) enableBulkSaveToDisk = containsImage;
//
//            if (containsImage)
//            {
//                Image image = imageCollection.getImage(imageKey);
//                //imageCollection.addImage(imageKey);
//                if ( image instanceof PerspectiveImage )
//                {
//                    PerspectiveImage pImage = (PerspectiveImage)imageCollection.getImage(imageKey);
//                    //image.setShowFrustum(showFrustumMenuItem.isSelected());
//                    if (!(image instanceof PerspectiveImage) || !((PerspectiveImage)image).isFrustumShowing())
//                        selectShowFrustum = false;
//                    if (imageKeys.size() == 1)
//                    {
//                        enableSimulateLighting = true;
//                        simulateLightingOn = pImage.isSimulatingLighingOn();
//                    }
//                    if (image.isVisible())
//                        selectHideImage = false;
//                } else
//                {
//                    if (!(image instanceof PerspectiveImage) || !((PerspectiveImage)image).isFrustumShowing())
//                        selectShowFrustum = false;
//                    if (imageKeys.size() == 1)
//                        enableSimulateLighting = true;
//                    if (image.isVisible())
//                        selectHideImage = false;
//                }
//            }
//            else
//            {
//                selectShowFrustum = false;
//                enableShowFrustum = false;
//                selectHideImage = false;
//                enableHideImage = false;
//            }
//
////            System.out.println("Image collection: " + imageCollection.getImages().iterator());
////            System.out.println("Image keys: " + imageKeys.size());
////            System.out.println("KetSet: " + keySet.size());
////            System.out.println("key info: " + imageKeys.get(0).source + imageKeys.get(0).name + imageKeys.get(0).band);
//
//
//            if (imageKey.getSource() == ImageSource.LOCAL_CYLINDRICAL || imageKey.getSource() == ImageSource.IMAGE_MAP)
//            {
//                enableMapBoundary = false;
//                enableShowFrustum = false;
//                enableSimulateLighting = false;
//                if (centerImageMenuItem != null)
//                    enableCenterImage = false;
//                enableSaveBackplanes = false;
//                enableSaveToDisk = false;
//
//                if (imageKey.getSource() == ImageSource.IMAGE_MAP)
//                {
//                    enableMapImage = false;
//                    enableHideImage = false;
//                }
//            }
//            else if (imageKey.getSource() == ImageSource.LOCAL_PERSPECTIVE)
//            {
////                enableSaveToDisk = false;
//                enableSaveToDisk = true;
//                enableSaveBackplanes = false;
//            }
//        }
//
//        if (enableBoundaryColor && imageKeys.size() > 0)
//        {
//            HashSet<String> colors = new HashSet<String>();
//            for (ImageKeyInterface imageKey : imageKeys)
//            {
//                int[] c = imageBoundaryCollection.getBoundary(imageKey).getBoundaryColor();
//                colors.add(c[0] + " " + c[1] + " " + c[2]);
//            }
//
//            // If the boundary color equals one of the predefined colors, then check
//            // the corresponding menu item.
//            int[] currentColor = imageBoundaryCollection.getBoundary(imageKeys.get(0)).getBoundaryColor();
//            for (JCheckBoxMenuItem item : colorMenuItems)
//            {
//                BoundaryColorAction action = (BoundaryColorAction)item.getAction();
//                Color color = action.color;
//                if (colors.size() == 1 &&
//                        currentColor[0] == color.getRed() &&
//                        currentColor[1] == color.getGreen() &&
//                        currentColor[2] == color.getBlue())
//                {
//                    item.setSelected(true);
//                }
//                else
//                {
//                    item.setSelected(false);
//                }
//            }
//        }
//
//        mapImageMenuItem.setSelected(selectMapImage);
//        mapImageMenuItem.setEnabled(enableMapImage);
//        mapBoundaryMenuItem.setSelected(selectMapBoundary);
//        mapBoundaryMenuItem.setEnabled(enableMapBoundary);
//        if (centerImageMenuItem != null)
//            centerImageMenuItem.setEnabled(enableCenterImage);
//        if (showImageInfoMenuItem != null)
//            showImageInfoMenuItem.setEnabled(enableShowImageInfo);
//        saveBackplanesMenuItem.setEnabled(enableSaveBackplanes);
//        saveToDiskMenuItem.setEnabled(enableSaveToDisk);
//        changeNormalOffsetMenuItem.setEnabled(enableChangeNormalOffset);
//        showFrustumMenuItem.setSelected(selectShowFrustum);
//        showFrustumMenuItem.setEnabled(enableShowFrustum);
//        exportInfofileMenuItem.setEnabled(enableSaveToDisk);
//        exportENVIImageMenuItem.setEnabled(enableSaveToDisk);
//        simulateLightingMenuItem.setEnabled(enableSimulateLighting);
//        simulateLightingMenuItem.setSelected(simulateLightingOn);
//        changeOpacityMenuItem.setEnabled(enableChangeOpacity);
//        hideImageMenuItem.setSelected(selectHideImage);
//        hideImageMenuItem.setEnabled(enableHideImage);
//        colorMenu.setEnabled(enableBoundaryColor);
//        exportFitsInfoPairsMenuItem.setEnabled(enableBulkSaveToDisk);
//    }
//
//
//
//    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
//            double[] pickedPosition)
//    {
//        if (pickedProp instanceof vtkActor)
//        {
//            if (imageBoundaryCollection != null && imageBoundaryCollection.getBoundary((vtkActor)pickedProp) != null)
//            {
//                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary((vtkActor)pickedProp);
//                setCurrentImage(boundary.getKey());
//                show(e.getComponent(), e.getX(), e.getY());
//            }
////            else if (imageCollection.getImage((vtkActor)pickedProp) != null)
//            else if (imageCollection.getImage((vtkActor)pickedProp).isPresent())
//            {
//                PerspectiveImage image = imageCollection.getImage((vtkActor)pickedProp).get();
//                setCurrentImage(image);
//                show(e.getComponent(), e.getX(), e.getY());
//            }
//
//            if (e.isShiftDown())
//            {
//                showImageInfoMenuItem.doClick();
//                setVisible(false);
//            }
//        }
//    }
//
//}
