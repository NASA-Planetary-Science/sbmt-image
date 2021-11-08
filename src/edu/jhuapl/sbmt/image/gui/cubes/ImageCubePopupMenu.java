package edu.jhuapl.sbmt.image.gui.cubes;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.core.Image;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeCollection;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundary;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.leisa.LEISAJupiterImage;
import edu.jhuapl.sbmt.model.mvic.MVICQuadJupiterImage;

import nom.tam.fits.FitsException;


public class ImageCubePopupMenu extends PopupMenu
{
    private Component invoker;
    private ImageCubeCollection imageCollection;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private List<ImageCubeKey> imageKeys = new ArrayList<ImageCubeKey>();
    private JMenuItem mapImageMenuItem;
    private JMenuItem mapBoundaryMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem showImageSpectrumMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem saveBackplanesMenuItem;
    private JMenuItem centerImageMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenuItem exportENVIImageMenuItem;
    private JMenuItem exportInfofileMenuItem;
    private JMenuItem changeNormalOffsetMenuItem;
    private JMenuItem simulateLightingMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideImageMenuItem;
    private JMenu colorMenu;
    private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customColorMenuItem;
    private SbmtInfoWindowManager infoPanelManager;
    private SbmtSpectrumWindowManager spectrumPanelManager;
    private Renderer renderer;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on the small body, 2 for right clicks on images
     * mapped to the small body.
     */
    public ImageCubePopupMenu(
            ImageCubeCollection imageCollection,
            PerspectiveImageBoundaryCollection imageBoundaryCollection,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            Renderer renderer,
            Component invoker)
    {
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;
        this.infoPanelManager = infoPanelManager;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        this.invoker = invoker;

        mapImageMenuItem = new JCheckBoxMenuItem(new MapImageAction());
        mapImageMenuItem.setText("Map Image Cube");
        this.add(mapImageMenuItem);

        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction());
        mapBoundaryMenuItem.setText("Map Image Boundary");
        this.add(mapBoundaryMenuItem);

        if (this.infoPanelManager != null)
        {
            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showImageInfoMenuItem.setText("Properties...");
            this.add(showImageInfoMenuItem);
        }

        if (this.spectrumPanelManager != null)
        {
            showImageSpectrumMenuItem = new JMenuItem(new ShowSpectrumAction());
            showImageSpectrumMenuItem.setText("Spectrum...");
//            this.add(showImageSpectrumMenuItem);
        }

        saveToDiskMenuItem = new JMenuItem(new SaveImageAction());
        saveToDiskMenuItem.setText("Save FITS Image...");
//        this.add(saveToDiskMenuItem);

        saveBackplanesMenuItem = new JMenuItem(new SaveBackplanesAction());
        saveBackplanesMenuItem.setText("Generate Backplanes...");
        this.add(saveBackplanesMenuItem);

        if (renderer != null)
        {
            centerImageMenuItem = new JMenuItem(new CenterImageAction());
            centerImageMenuItem.setText("Center in Window");
            this.add(centerImageMenuItem);
        }

        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction());
        showFrustumMenuItem.setText("Show Frustum");
        this.add(showFrustumMenuItem);

        exportENVIImageMenuItem = new JMenuItem(new ExportENVIImageAction()); // twupy1
        exportENVIImageMenuItem.setText("Export ENVI Image...");
        this.add(exportENVIImageMenuItem);

        exportInfofileMenuItem = new JMenuItem(new ExportInfofileAction());
        exportInfofileMenuItem.setText("Export INFO File...");
        this.add(exportInfofileMenuItem);

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);

        simulateLightingMenuItem = new JMenuItem(new SimulateLightingAction());
        simulateLightingMenuItem.setText("Simulate Lighting");
        this.add(simulateLightingMenuItem);

        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction());
        changeOpacityMenuItem.setText("Change Opacity...");
        this.add(changeOpacityMenuItem);

        hideImageMenuItem = new JCheckBoxMenuItem(new HideImageAction());
        hideImageMenuItem.setText("Hide Image");
        this.add(hideImageMenuItem);

        colorMenu = new JMenu("Boundary Color");
        this.add(colorMenu);
        for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
        {
            JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new BoundaryColorAction(color.color()));
            colorMenuItems.add(colorMenuItem);
            colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
            colorMenu.add(colorMenuItem);
        }
        colorMenu.addSeparator();
        customColorMenuItem = new JMenuItem(new CustomBoundaryColorAction());
        customColorMenuItem.setText("Custom...");
        colorMenu.add(customColorMenuItem);

    }

    public void setCurrentImage(ImageCubeKey key)
    {
        imageKeys.clear();
        imageKeys.add(key);

        updateMenuItems();
    }

    public void setCurrentImages(List<ImageCubeKey> keys)
    {
        imageKeys.clear();
        imageKeys.addAll(keys);

        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean selectMapImage = true;
        boolean enableMapImage = true;
        boolean selectMapBoundary = true;
        boolean enableMapBoundary = true;
        boolean enableCenterImage = false;
        boolean enableShowImageInfo = false;
        boolean enableSaveBackplanes = false;
        boolean enableSaveToDisk = false;
        boolean enableChangeNormalOffset = false;
        boolean selectShowFrustum = true;
        boolean enableShowFrustum = true;
        boolean enableSimulateLighting = false;
        boolean enableChangeOpacity = false;
        boolean selectHideImage = true;
        boolean enableHideImage = true;
        boolean enableBoundaryColor = true;

        for (ImageCubeKey imageKey : imageKeys)
        {
            boolean containsImage = imageCollection.containsImage(imageKey);
            boolean containsBoundary = false;
            if (imageBoundaryCollection != null)
                containsBoundary = imageBoundaryCollection.containsBoundary(imageKey);

            if (!containsBoundary)
            {
                selectMapBoundary = containsBoundary;
                enableBoundaryColor = false;
            }

            if (!containsImage)
                selectMapImage = containsImage;

            if (centerImageMenuItem != null && imageKeys.size() == 1)
            {
                enableCenterImage = containsBoundary || containsImage;
            }

            if (showImageInfoMenuItem != null && imageKeys.size() == 1)
                enableShowImageInfo = containsImage;

            if (imageKeys.size() == 1)
            {
                enableSaveBackplanes = containsImage;
                enableSaveToDisk = containsImage;
                enableChangeNormalOffset = containsImage;
                enableChangeOpacity = containsImage;
            }

            if (containsImage)
            {
                Image image = imageCollection.getImage(imageKey);
                if (!(image instanceof PerspectiveImage) || !((PerspectiveImage)image).isFrustumShowing())
                    selectShowFrustum = false;
                if (imageKeys.size() == 1)
                    enableSimulateLighting = true;
                if (image.isVisible())
                    selectHideImage = false;
            }
            else
            {
                selectShowFrustum = false;
                enableShowFrustum = false;
                selectHideImage = false;
                enableHideImage = false;
            }

            if (imageKey.source == ImageSource.LOCAL_CYLINDRICAL || imageKey.source == ImageSource.IMAGE_MAP)
            {
                enableMapBoundary = false;
                enableShowFrustum = false;
                enableSimulateLighting = false;
                if (centerImageMenuItem != null)
                    enableCenterImage = false;
                enableSaveBackplanes = false;
                enableSaveToDisk = false;

                if (imageKey.source == ImageSource.IMAGE_MAP)
                {
                    enableMapImage = false;
                    enableHideImage = false;
                }
            }
            else if (imageKey.source == ImageSource.LOCAL_PERSPECTIVE)
            {
//                enableSaveToDisk = false;
                enableSaveToDisk = true;
                enableSaveBackplanes = false;
            }
        }

        if (enableBoundaryColor)
        {
            HashSet<String> colors = new HashSet<String>();
            for (ImageCubeKey imageKey : imageKeys)
            {
                int[] c = imageBoundaryCollection.getBoundary(imageKey).getBoundaryColor();
                colors.add(c[0] + " " + c[1] + " " + c[2]);
            }

            // If the boundary color equals one of the predefined colors, then check
            // the corresponding menu item.
            int[] currentColor = imageBoundaryCollection.getBoundary(imageKeys.get(0)).getBoundaryColor();
            for (JCheckBoxMenuItem item : colorMenuItems)
            {
                BoundaryColorAction action = (BoundaryColorAction)item.getAction();
                Color color = action.color;
                if (colors.size() == 1 &&
                        currentColor[0] == color.getRed() &&
                        currentColor[1] == color.getGreen() &&
                        currentColor[2] == color.getBlue())
                {
                    item.setSelected(true);
                }
                else
                {
                    item.setSelected(false);
                }
            }
        }

        mapImageMenuItem.setSelected(selectMapImage);
        mapImageMenuItem.setEnabled(enableMapImage);
        mapBoundaryMenuItem.setSelected(selectMapBoundary);
        mapBoundaryMenuItem.setEnabled(enableMapBoundary);
        if (centerImageMenuItem != null)
            centerImageMenuItem.setEnabled(enableCenterImage);
        if (showImageInfoMenuItem != null)
            showImageInfoMenuItem.setEnabled(enableShowImageInfo);
        saveBackplanesMenuItem.setEnabled(enableSaveBackplanes);
        saveToDiskMenuItem.setEnabled(enableSaveToDisk);
        changeNormalOffsetMenuItem.setEnabled(enableChangeNormalOffset);
        showFrustumMenuItem.setSelected(selectShowFrustum);
        showFrustumMenuItem.setEnabled(enableShowFrustum);
        exportENVIImageMenuItem.setEnabled(enableSaveToDisk);
        simulateLightingMenuItem.setEnabled(enableSimulateLighting);
        changeOpacityMenuItem.setEnabled(enableChangeOpacity);
        hideImageMenuItem.setSelected(selectHideImage);
        hideImageMenuItem.setEnabled(enableHideImage);
        colorMenu.setEnabled(enableBoundaryColor);
    }

    public class MapImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
//            System.out.println("MapImageAction.actionPerformed()");
            for (ImageCubeKey imageKey : imageKeys)
            {
                try
                {
                    if (mapImageMenuItem.isSelected())
                        imageCollection.addImage(imageKey);
                    else
                        imageCollection.removeImage(imageKey);
                }
                catch (FitsException e1) {
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class MapBoundaryAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageCubeKey imageKey : imageKeys)
            {
                try
                {
                    if (mapBoundaryMenuItem.isSelected())
                        imageBoundaryCollection.addBoundary(imageKey);
                    else
                        imageBoundaryCollection.removeBoundary(imageKey);
                }
                catch (FitsException e1) {
                    e1.printStackTrace();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            try
            {
                imageCollection.addImage(imageKey);
                infoPanelManager.addData(imageCollection.getImage(imageKey));

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private class ShowSpectrumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            try
            {
                imageCollection.addImage(imageKey);
                Image image = imageCollection.getImage(imageKey);
                if (image instanceof LEISAJupiterImage || image instanceof MVICQuadJupiterImage)
                    spectrumPanelManager.addData(imageCollection.getImage(imageKey));

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private class SaveImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            File file = null;
            try
            {
                imageCollection.addImage(imageKey);
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                String path = image.getFitFileFullPath();
                String extension = path.substring(path.lastIndexOf("."));
                String imageFileName = new File(path).getName();

                file = CustomFileChooser.showSaveDialog(invoker, "Save FITS image", imageFileName, "fit");
                if (file != null)
                {
                    File fitFile = FileCache.getFileFromServer(imageKey.name + extension);

                    FileUtil.copyFile(fitFile, file);
                }
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private class CenterImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            double[] spacecraftPosition = new double[3];
            double[] focalPoint = new double[3];
            double[] upVector = new double[3];
            double viewAngle = 0.0;

            if (imageBoundaryCollection != null && imageBoundaryCollection.containsBoundary(imageKey))
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
                boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);

                viewAngle = boundary.getImage().getMaxFovAngle();
            }
            else if (imageCollection.containsImage(imageKey))
            {
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);

                viewAngle = image.getMaxFovAngle();
            }
            else
            {
                return;
            }

            renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
        }
    }

    private class SaveBackplanesAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            // First generate the DDR

            String defaultFilename = new File(imageKey.name + "_DDR.IMG").getName();
            File file = CustomFileChooser.showSaveDialog(invoker, "Save Backplanes DDR", defaultFilename, "img");

            try
            {
                if (file != null)
                {
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

                    updateMenuItems();

                    float[] backplanes = image.generateBackplanes();

                    byte[] buf = new byte[4];
                    for (int i=0; i<backplanes.length; ++i)
                    {
                        int v = Float.floatToIntBits(backplanes[i]);
                        buf[0] = (byte)(v >>> 24);
                        buf[1] = (byte)(v >>> 16);
                        buf[2] = (byte)(v >>>  8);
                        buf[3] = (byte)(v >>>  0);
                        out.write(buf, 0, buf.length);
                    }

                    out.close();
                }
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

            // Then generate the LBL file using the same filename but with a lbl extension.
            // The extension is chosen to have the same case as the img file.

            try
            {
                if (file != null)
                {
//                    String imgName = file.getName();
                    File imgName = file;

                    String lblName = file.getAbsolutePath();
                    lblName = lblName.substring(0, lblName.length()-4);
//                    if (file.getAbsolutePath().endsWith("img"))
//                        lblName += ".lbl";
//                    else
//                        lblName += ".LBL";

                    File labelFile = new File(lblName);

                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

                    updateMenuItems();

                    image.generateBackplanesLabel(imgName, labelFile);
                }
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

        }
    }

    private class ShowFrustumAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageCubeKey imageKey : imageKeys)
            {
                try
                {
                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                    image.setShowFrustum(showFrustumMenuItem.isSelected());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class ExportENVIImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            // Only works for a single image (for now)
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            File file = null;
            try
            {
                // Get the PerspectiveImage
                imageCollection.addImage(imageKey);
                PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);

                // Default name
                String fullPathName = image.getFitFileFullPath();
                if (fullPathName == null)
                    fullPathName = image.getPngFileFullPath();
                if (fullPathName == null)
                    fullPathName = image.getSumfileFullPath();
                String imageFileName = fullPathName != null ? new File(fullPathName).getName() : null;

                String defaultFileName = null;
                if (imageFileName != null)
                    defaultFileName = imageFileName.substring(0, imageFileName.length()-4);
                else
                    defaultFileName = ((ImageCubeKey)image.getKey()).name;

                // Open save dialog
                file = CustomFileChooser.showSaveDialog(invoker, "Export ENVI image as", defaultFileName + ".hdr", "hdr");
                if (file != null)
                {
                    String filename = file.getAbsolutePath();
                    image.exportAsEnvi(filename.substring(0, filename.length()-4), "bsq", true);
                }
            }
            catch(Exception ex)
            {
                // Something went wrong during the file conversion/export process
                // (after save dialog has returned)
                String path = file != null ? file.getAbsolutePath() : "null";
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to export ENVI image as " + file.getAbsolutePath(),
                        "Error Exporting ENVI Image",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private class ExportInfofileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageCubeKey imageKey : imageKeys)
            {
                try
                {
                    imageCollection.addImage(imageKey);
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
                    String fullPathName = image.getFitFileFullPath();
                    if (fullPathName == null)
                        fullPathName = image.getPngFileFullPath();
                    String imageFileName = new File(fullPathName).getName();


                    String defaultFileName = null;
                    if (imageFileName != null)
                        defaultFileName = imageFileName.substring(0, imageFileName.length() - FilenameUtils.getExtension(imageFileName).length()) + "INFO";

                    File file = CustomFileChooser.showSaveDialog(invoker, "Save INFO file as...", defaultFileName);
                    if (file == null)
                    {
                        return;
                    }

                    String filename = file.getAbsolutePath();

//                    System.out.println("Exporting INFO file for " + image.getImageName() + " to " + filename);

                    image.saveImageInfo(filename);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class ChangeNormalOffsetAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            Image image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
            }
        }
    }

    private class SimulateLightingAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
            if (image != null)
            {
                double[] sunDir = image.getSunVector();
                renderer.setLightCfgToFixedLightAtDirection(new Vector3D(sunDir));
            }
        }
    }

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (imageKeys.size() != 1)
                return;
            ImageCubeKey imageKey = imageKeys.get(0);

            Image image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                OpacityChanger opacityChanger = new OpacityChanger(image);
                opacityChanger.setLocationRelativeTo(renderer);
                opacityChanger.setVisible(true);
            }
        }
    }

    private class HideImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ImageCubeKey imageKey : imageKeys)
            {
                try
                {
                    imageCollection.addImage(imageKey);
                    Image image = imageCollection.getImage(imageKey);
                    image.setVisible(!hideImageMenuItem.isSelected());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            updateMenuItems();
        }
    }

    private class BoundaryColorAction extends AbstractAction
    {
        private Color color;

        public BoundaryColorAction(Color color)
        {
            this.color = color;
        }

        public void actionPerformed(ActionEvent e)
        {
            for (ImageCubeKey imageKey : imageKeys)
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
                boundary.setBoundaryColor(color);
            }

            updateMenuItems();
        }
    }

    private class CustomBoundaryColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKeys.get(0));
            int[] currentColor = boundary.getBoundaryColor();
            Color newColor = ColorChooser.showColorChooser(invoker, currentColor);
            if (newColor != null)
            {
                for (ImageCubeKey imageKey : imageKeys)
                {
                    boundary = imageBoundaryCollection.getBoundary(imageKey);
                    boundary.setBoundaryColor(newColor);
                }
            }
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (imageBoundaryCollection != null && imageBoundaryCollection.getBoundary((vtkActor)pickedProp) != null)
            {
                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary((vtkActor)pickedProp);
                setCurrentImage((ImageCubeKey)boundary.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
            else if (imageCollection.getImage((vtkActor)pickedProp) != null)
            {
                Image image = imageCollection.getImage((vtkActor)pickedProp);
                setCurrentImage((ImageCubeKey)image.getKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
