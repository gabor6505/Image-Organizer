package net.gabor6505.imageorganizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

    public ImageOrganizer() {
        super();
        setupLookAndFeel();

        setTitle(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        setMinimumSize(new Dimension(320, 160));
        setSize(1620, 950);
        setLocationRelativeTo(null);
        setVisible(true);

        setupComponents();
    }

    private void setupComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        setContentPane(panel);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(Color.GRAY);
        panel.add(centerPanel, BorderLayout.CENTER);

        imageLabel = new ImageLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Utils.fixSize(imageLabel, 1600, 900);
        centerPanel.add(imageLabel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        panel.add(northPanel, BorderLayout.NORTH);

        selectedFolderLabel = new JLabel("No folder is selected");
        selectedFolderLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        northPanel.add(selectedFolderLabel, BorderLayout.CENTER);

        JPanel eastNorthPanel = new JPanel();
        northPanel.add(eastNorthPanel, BorderLayout.EAST);

        JCheckBox cacheCheckBox = new JCheckBox("Cache Images");
        cacheCheckBox.addItemListener(e -> {
            cacheImages = cacheCheckBox.isSelected();
            System.out.println("Caching turned " + (cacheImages ? "on " : "off"));
            if (cacheImages && imageCache != null) imageCache.execute();
        });
        eastNorthPanel.add(cacheCheckBox);

        JButton selectFolderBtn = new JButton("Select...");
        selectFolderBtn.addActionListener(e -> selectFolder());
        eastNorthPanel.add(selectFolderBtn);

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
        panel.add(cacheProgressBar, BorderLayout.PAGE_END);

        panel.revalidate();
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
        imageIndex = -1;
        loadImages();
    }

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

        imageCache = new ImageCacheManager(imageNames, workFolder, cacheProgressBar);
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
        if (direction) {
            if (imageNames.size() > imageIndex + 1) imageIndex++;
            else return;
        } else {
            if (imageIndex > 0) imageIndex--;
            else return;
        }

        System.out.println("Stepping to image #" + imageIndex + "!");

        if (imageCache != null) {
            imageLabel.setIcon(imageCache.requestImage(imageIndex, 1600, 900));
            System.out.println("Stepped to image #" + imageIndex + "!");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED) return false;

        // TODO shortcut editor window for specifying the association between folders and keys (which key moves image to which folder)

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                stepImage(false);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                stepImage(true);
                break;
        }

        return false;
    }
}
