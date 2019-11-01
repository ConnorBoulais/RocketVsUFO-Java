import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ImageLoad {

    private static final int COLOR_MASK = 0xffffff;
    private static final int KEYED_IMAGE_MIN = 5;
    private static final int KEYED_RED_IDX = 2;
    private static final int KEYED_GREEN_IDX = 3;
    private static final int KEYED_BLUE_IDX = 4;

    public static Background createDefaultBackground(ImageStore imageStore)
    {
        return new Background(VirtualWorld.DEFAULT_IMAGE_NAME,
                imageStore.getImageList(VirtualWorld.DEFAULT_IMAGE_NAME));
    }

    public static PImage createImageColored(int width, int height, int color)
    {
        PImage img = new PImage(width, height, VirtualWorld.RGB);
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++)
        {
            img.pixels[i] = color;
        }
        img.updatePixels();
        return img;
    }

    public static void loadImages(String filename, ImageStore imageStore, PApplet screen)
    {
        try
        {
            Scanner in = new Scanner(new File(filename));
            loadImages(in, screen, imageStore);
        }
        catch (FileNotFoundException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private static void setAlpha(PImage img, int maskColor, int alpha)
    {
        int alphaValue = alpha << 24;
        int nonAlpha = maskColor & COLOR_MASK;
        img.format = PApplet.ARGB;
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++)
        {
            if ((img.pixels[i] & COLOR_MASK) == nonAlpha)
            {
                img.pixels[i] = alphaValue | nonAlpha;
            }
        }
        img.updatePixels();
    }

    private static List<PImage> getImages(Map<String, List<PImage>> images,
                                          String key)
    {
        List<PImage> imgs = images.get(key);
        if (imgs == null)
        {
            imgs = new LinkedList<>();
            images.put(key, imgs);
        }
        return imgs;
    }

    private static void processImageLine(Map<String, List<PImage>> images,
                                        String line, PApplet screen)
    {
        String[] attrs = line.split("\\s");
        if (attrs.length >= 2)
        {
            String key = attrs[0];
            PImage img = screen.loadImage(attrs[1]);
            if (img != null && img.width != -1)
            {
                List<PImage> imgs = getImages(images, key);
                imgs.add(img);

                if (attrs.length >= KEYED_IMAGE_MIN)
                {
                    int r = Integer.parseInt(attrs[KEYED_RED_IDX]);
                    int g = Integer.parseInt(attrs[KEYED_GREEN_IDX]);
                    int b = Integer.parseInt(attrs[KEYED_BLUE_IDX]);
                    setAlpha(img, screen.color(r, g, b), 0);
                }
            }
        }
    }

    public static int clamp(int value, int low, int high)
    {
        return Math.min(high, Math.max(value, low));
    }

    private static void loadImages(Scanner in, PApplet screen, ImageStore imageStore)
    {
        int lineNumber = 0;
        while (in.hasNextLine())
        {
            try
            {
                ImageLoad.processImageLine(imageStore.getImages(), in.nextLine(), screen);
            }
            catch (NumberFormatException e)
            {
                System.out.println(String.format("Image format error on line %d",
                        lineNumber));
            }
            lineNumber++;
        }
    }
}
