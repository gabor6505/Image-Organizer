package net.gabor6505.imageorganizer;

import net.gabor6505.imageorganizer.components.ImageLabel;
import net.gabor6505.imageorganizer.components.KeyBindConfigWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.swing.JFileChooser.APPROVE_OPTION;

public class ImageOrganizer extends JFrame implements KeyEventDispatcher {

    private final String TITLE = "Image Organizer";

    private String workFolder = "";
    private boolean cacheImages = false;
    private int imageIndex = -1;

    private JLabel selectedFolderLabel;
    private ImageLabel imageLabel;
    private JProgressBar cacheProgressBar;

    private final List<String> imageNames = new ArrayList<>();
    private final List<String> imageFolders = new ArrayList<>();
    private ImageCacheManager imageCache = null;

    private KeyBindConfigWindow configWindow = null;
    private final AtomicBoolean keyBindWindowVisible = new AtomicBoolean(false);

    public ImageOrganizer() {
        super();
        setupLookAndFeel();

        setTitle(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        setMinimumSize(new Dimension(320, 160));
        setSize(1620, 975);
        setLocationRelativeTo(null);

        setupComponents();

        setVisible(true);
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.GRAY);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        {
            // IMAGE LABEL
            imageLabel = new ImageLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerPanel.add(imageLabel, BorderLayout.CENTER);
        }

        JPanel northPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanel, BorderLayout.NORTH);
        {
            // SELECTED FOLDER LABEL
            selectedFolderLabel = new JLabel("No folder is selected");
            selectedFolderLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            northPanel.add(selectedFolderLabel, BorderLayout.CENTER);

            JPanel eastNorthPanel = new JPanel();
            northPanel.add(eastNorthPanel, BorderLayout.EAST);
            {
                // CACHE CHECKBOX
                JCheckBox cacheCheckBox = new JCheckBox("Cache Images");
                cacheCheckBox.addItemListener(e -> {
                    cacheImages = cacheCheckBox.isSelected();
                    System.out.println("Caching turned " + (cacheImages ? "on " : "off"));
                    if (cacheImages && imageCache != null) imageCache.executeTask();
                });
                eastNorthPanel.add(cacheCheckBox);

                // SELECT BUTTON
                JButton selectFolderBtn = new JButton("Select...");
                selectFolderBtn.addActionListener(e -> selectFolder());
                eastNorthPanel.add(selectFolderBtn);

                // KEYBIND CONFIG BUTTON
                JButton keyBindCfgBtn = new JButton("Keybinds...");
                keyBindCfgBtn.addActionListener(e -> keyBindConfig());
                eastNorthPanel.add(keyBindCfgBtn);
            }
        }

        // PROGRESS BAR
        cacheProgressBar = new JProgressBar() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 30);
            }
        };
        cacheProgressBar.setMaximumSize(new Dimension(200, 50));
        cacheProgressBar.setSize(new Dimension(200, 50));
        cacheProgressBar.setIndeterminate(false);
        cacheProgressBar.setStringPainted(true);
        cacheProgressBar.setVisible(false);
        mainPanel.add(cacheProgressBar, BorderLayout.PAGE_END);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().equals("Nimbus")) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored2) {
            }
        }
    }

    private void selectFolder() {
        // Use FileDialog if OS is Mac OS (because it looks native) and JFileChooser if OS is not Mac OS
        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            FileDialog dialog = new FileDialog(this, "Choose a folder containing images", FileDialog.LOAD);
            dialog.setDirectory(workFolder.isEmpty() ? System.getProperty("user.home") : new File(workFolder).getParent());
            dialog.setVisible(true);

            if (dialog.getFile() != null && dialog.getDirectory() != null) {
                File directory = new File(dialog.getDirectory() + dialog.getFile());
                if (directory.isDirectory()) {
                    processFolderSelection(directory);
                }
            }
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
        } else {
            JFileChooser chooser = new JFileChooser(workFolder.isEmpty() ? System.getProperty("user.home") : new File(workFolder).getParent()) {
                public void approveSelection() {
                    if (!getSelectedFile().isFile()) super.approveSelection();
                }
            };
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(false);
            chooser.setDialogTitle("Choose a folder containing images");
            int returnValue = chooser.showDialog(this, "OK");

            if (returnValue == APPROVE_OPTION) {
                if (chooser.getSelectedFile() != null) {
                    File directory = chooser.getSelectedFile();
                    if (directory.isDirectory()) {
                        processFolderSelection(directory);
                    }
                }
            }
        }
    }

    private void processFolderSelection(File directory) {
        if (workFolder.equals(directory.getPath())) return;
        workFolder = directory.getPath();
        selectedFolderLabel.setText("Selected folder: " + workFolder);
        imageNames.clear();
        imageFolders.clear();
        imageIndex = -1;
        loadImages();
    }

    // TODO also load images from the 1st level subfolders (so that images previously moved can still be modified after app restart) - this should be a toggleable setting
    private void loadImages() {
        System.out.println("Loading image names...");

        File dir = new File(workFolder);
        if (!dir.isDirectory()) {
            System.err.println("Image loading failed: the path does not point to a directory!");
            return;
        }

        File[] images = dir.listFiles((d, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
        if (images == null) return;

        Arrays.sort(images, Comparator.comparingLong(File::lastModified));

        for (File f : images) {
            imageNames.add(f.getName());
            imageFolders.add("");
            System.out.println(f.getName());
        }

        imageFolders.addAll(Collections.nCopies(imageNames.size(), ""));

        imageCache = new ImageCacheManager(imageNames, imageFolders, workFolder, cacheProgressBar);
        if (cacheImages) imageCache.executeTask();

        // Initial step from -1 to 0 position
        stepImage(true);
    }

    /**
     * Steps the images in either the left or the right direction
     *
     * @param direction The direction of the step, false meaning left and true meaning right
     */
    private void stepImage(boolean direction) {
        // Calculate new index
        if (direction) {
            if (imageNames.size() > imageIndex + 1) imageIndex++;
            else return;
        } else {
            if (imageIndex > 0) imageIndex--;
            else return;
        }

        // Request new image and set it as the icon for the label
        if (imageCache != null) {
            //imageLabel.setIcon(imageCache.requestImage(imageIndex, 1600, 900));
            imageLabel.setImage(imageCache.requestRawImage(imageIndex));
            System.out.println("Stepped to image #" + imageIndex + "!");
        }

        updateTitle();
    }

    private void keyBindConfig() {
        if (keyBindWindowVisible.get()) {
            if (configWindow != null) configWindow.requestFocus();
        } else {
            keyBindWindowVisible.set(true);
            configWindow = new KeyBindConfigWindow(this, keyBindWindowVisible);
        }
    }

    private void moveImage(String destFolder) {
        if (destFolder.isEmpty()) return;
        File destDir = new File(workFolder + File.separator + destFolder);
        if (!destDir.exists()) if (!destDir.mkdirs()) System.err.println("Failed to create dirs for image move!");

        File currentImage;
        if (imageFolders.get(imageIndex).isEmpty()) {
            currentImage = new File(workFolder + File.separator + imageNames.get(imageIndex));
        } else {
            currentImage = new File(workFolder + File.separator + imageFolders.get(imageIndex) + File.separator + imageNames.get(imageIndex));
        }

        if (!currentImage.renameTo(new File(destDir.getPath() + File.separator + imageNames.get(imageIndex)))) {
            System.err.println("Failed to move image!");
        } else {
            imageFolders.set(imageIndex, destFolder);
            updateTitle();
        }
    }

    private void updateTitle() {
        // Display new image name and folder in the title
        String format = "%s - %s";
        if (!imageFolders.get(imageIndex).isEmpty()) format += " - Moved to %s";
        setTitle(String.format(format, TITLE, imageNames.get(imageIndex), imageFolders.get(imageIndex)));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED || !isFocused() || imageNames.size() < 1) return false;

        // TODO Implement some kind of way to delete images (either to the recycle bin or a trash folder)

        for (Integer keyCode : PreferenceManager.getKeyBindMap().getKeyCodes()) {
            if (e.getKeyCode() == keyCode) {
                moveImage(PreferenceManager.getKeyBindMap().get(keyCode));
                return true;
            }
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                stepImage(false);
                return true;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                stepImage(true);
                return true;
        }

        return false;
    }
}
