package net.gabor6505.imageorganizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO only cache the images that are close to the currently displayed image, to reduce RAM consumption
public class ImageCacheManager extends SwingWorker<List<BufferedImage>, Integer> {

    private final List<String> imageNames;
    private final List<String> imageFolders;
    private String workFolder;
    private final JProgressBar progressBar;

    private final List<BufferedImage> cache;
    private final List<Boolean> alreadyLoading;

    private static int LOAD_COUNT = -1;
    private final int loadID;

    private boolean alreadyExecuted = false;

    public ImageCacheManager(List<String> imageNames, List<String> imageFolders, String workFolder, JProgressBar progressBar) {
        this.imageNames = imageNames;
        this.imageFolders = imageFolders;
        this.workFolder = workFolder;
        this.progressBar = progressBar;

        cache = new ArrayList<>(Collections.nCopies(imageNames.size(), null));
        alreadyLoading = new ArrayList<>(Collections.nCopies(imageNames.size(), false));

        LOAD_COUNT++;
        loadID = LOAD_COUNT;
    }

    public void executeTask() {
        if (alreadyExecuted) return;
        alreadyExecuted = true;
        progressBar.setVisible(true);
        progressBar.getParent().revalidate();
        progressBar.setMaximum(imageNames.size());
        progressBar.setValue(0);
        execute();
    }

    @Override
    protected List<BufferedImage> doInBackground() {
        System.out.println("Caching " + imageNames.size() + " image(s)...");
        for (int i = 0; i < imageNames.size(); i++) {
            if (loadID != LOAD_COUNT) return null;
            if (cache.get(i) != null) {
                System.out.println("Image " + imageNames.get(i) + " is already cached, skipping.");
                publish(i + 1);
                continue;
            }

            try {
                if (alreadyLoading.get(i)) {
                    System.out.println("Image " + imageNames.get(i) + " is already reserved for loading, skipping.");
                    publish(i + 1);
                    continue;
                }
                cache.set(i, ImageIO.read(new File(workFolder + File.separator + imageNames.get(i))));
            } catch (IOException e) {
                System.err.println("Error occurred while caching!");
            }
            System.out.println("Caching " + imageNames.get(i));
            publish(i + 1);
        }
        return cache;
    }

    @Override
    protected void process(List<Integer> chunks) {
        progressBar.setValue(chunks.get(0));
        progressBar.setString(imageNames.get(chunks.get(0) - 1));
    }

    @Override
    protected void done() {
        progressBar.setVisible(false);
        progressBar.getParent().revalidate();
        System.out.println("Images successfully cached!");
    }

    public List<BufferedImage> getCache() {
        return cache;
    }

    /**
     * Request a smooth-scaled image icon from the cache ready to be used on JLabels
     * If the image at the requested index is not yet cached, it gets cached and returned by a high priority background operation
     *
     * @param index The index of the image in the cache
     * @param width The width that the image should be scaled to
     * @param height The height that the image should be scaled to
     * @return The scaled version of the requested image, either from the cache or from disk, or null if the index is out of bounds
     */
    public ImageIcon requestImage(int index, int width, int height) {
        BufferedImage rawImage = requestRawImage(index);
        return new ImageIcon(rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public BufferedImage requestRawImage(int index) {
        if (cache.size() <= index) return null;
        if (cache.get(index) == null) {
            alreadyLoading.set(index, true);
            try {
                cache.set(index, ImageIO.read(new File(workFolder + File.separator + imageNames.get(index))));
            } catch (IOException e) {
                System.err.println("Error occurred while caching high priority image!");
            }
        }
        if (cache.get(index) == null) return null;
        return cache.get(index);
    }
}
