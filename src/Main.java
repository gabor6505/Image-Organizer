import net.gabor6505.imageorganizer.ImageOrganizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Main extends JFrame implements KeyEventDispatcher {

    private final String WORK_FOLDER = "C:\\Users\\gabor\\OneDrive\\Képek\\Képernyőképek\\2019-05";

    private final String TITLE = "Image Organizer";

    private final ImageLabel imageLabel;
    private final List<String> imageNameList = new ArrayList<>();
    private final List<BufferedImage> rawImages = new ArrayList<>();
    private int currentIndex = 0;

    public static void main(String[] args) {
        new ImageOrganizer();
    }

    public Main() {
        super();

        JPanel panel = new JPanel();
        panel.setAlignmentX(CENTER_ALIGNMENT);
        panel.setAlignmentY(CENTER_ALIGNMENT);
        setContentPane(panel);

        imageLabel = new ImageLabel("", 1600, 900, false);
        panel.add(imageLabel);

        if (imageNameList.size() == 0) return;
        updateImage();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED) return false;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                if (currentIndex == 0) break;
                currentIndex--;
                updateImage();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                if (imageNameList.size() <= currentIndex + 1) break;
                currentIndex++;
                updateImage();
                break;
            case KeyEvent.VK_T:
                moveImage("Studying");
                break;
            case KeyEvent.VK_S:
                moveImage("Social");
                break;
            case KeyEvent.VK_G:
                moveImage("Gaming");
                break;
            case KeyEvent.VK_M:
                moveImage("Maintenance");
                break;
        }

        return false;
    }

    private void updateImage() {
        String imageName = imageNameList.get(currentIndex);

        imageLabel.updateImage(WORK_FOLDER + File.separator + imageName, false);
        System.out.println("Updated image path: " + WORK_FOLDER + File.separator + imageName);

        if (imageName.contains(File.separator))
            setTitle(String.format("%s (%s) [MOVED]", TITLE, imageName.replaceAll(".+\\\\", "")));
        else
            setTitle(String.format("%s (%s)", TITLE, imageName));
    }

    private void moveImage(String destinationFolderName) {
        File dir = new File(WORK_FOLDER + File.separator + destinationFolderName);
        if (!dir.exists()) if (!dir.mkdir()) return;

        String imageName = imageNameList.get(currentIndex);
        String newFileName = destinationFolderName + File.separator + imageNameList.get(currentIndex);

        boolean success = new File(WORK_FOLDER + File.separator + imageNameList.get(currentIndex)).renameTo(new File(WORK_FOLDER + File.separator + newFileName));

        if (!success) return;
        imageNameList.set(currentIndex, newFileName);
        System.out.println("Moved image into folder: " + destinationFolderName);
        setTitle(String.format("%s (%s) [MOVED]", TITLE, imageName));
    }
}
