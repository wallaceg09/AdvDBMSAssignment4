import java.util.Scanner;
import java.nio.file.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageCompare {
    
    public static void main(String[] args) {
        List<double[]> bucket_list = new ArrayList<double[]>();
        HashMap<Integer, String> imageIDS = new HashMap<>();
        HashMap<Integer, Double> distance_list = new HashMap<>();
        double[] query_image_bucket = new double[64];
                     
        Scanner scan = new Scanner(System.in);
       /* System.out.println("Enter the directory containing the collection of images.");
        System.out.println("Example: C:/users/my_folder/my_directory");
        System.out.println("Enter directory path: ");*/
        String path = args[0];
        
        //System.out.println("Enter the complete file path and file name of the query image.");
        String query_image = args[1];
        
        //System.out.println("Enter K, the Kth most similar images to display.");
        int k = Integer.parseInt(args[2]);
        
        Path dir = Paths.get(path);
        
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir))
        {   
            int image_count = 0;
            BufferedImage main_image = ImageIO.read(new File(query_image));
            query_image_bucket = readPixels(main_image);
            
            for(Path file: stream)
            {
                String file_path = path + "\\" + file.getFileName();
                File f = new File(file_path);
                BufferedImage image = ImageIO.read(f);
                if(image != null)
                {
                    bucket_list.add(readPixels(image));
                    imageIDS.put(image_count, "" + file.getFileName());
                }
                
                image_count++;
            }
        }
        catch(IOException | DirectoryIteratorException e)
        {
            System.err.println(e);
        }
        
        for(int i=0; i<bucket_list.size(); i++)
        {
            double distance = imageDistances(query_image_bucket, bucket_list.get(i));
            distance_list.put(i,distance);
        }
        
        int n = 1;
        System.out.println();
        double max = 0;
        int max_index = 0;
        for(int i=0; i<distance_list.size(); i++)
        {
            if(distance_list.get(i) > max)
            {
                max = distance_list.get(i);
                max_index = i;
            }
        }
        
        while(k>0)
        {
            int index = max_index;
            double min = max;
            
            for(int i=0; i<distance_list.size(); i++)
            {
                if(distance_list.containsKey(i) && distance_list.get(i) < min)
                {
                    min = distance_list.get(i);
                    index = i;
                }
            }
            System.out.println(n + ". " + imageIDS.get(index));
            imageIDS.remove(index);
            distance_list.remove(index);
            
            k--;
            n++;
        }
    }
    
   
    public static double[] readPixels(BufferedImage image)
    {
        int[] color_bucket = new int[64];
        double[] color_bucket_percent = new double[64];
        
        for(int i=0; i<64; i++)
            color_bucket[i] = 0;
                
        int h = image.getHeight();
        int w = image.getWidth();
        int num_pixels = h*w;
                
        int[] pixel;   
        for(int i=0; i<h; i++)
        {
            for(int j=0; j<w; j++)
            {
                pixel = image.getRaster().getPixel(j,i,new int[3]);
                int red = pixel[0];
                int green = pixel[1];
                int blue = pixel[2]; 
                
                int bucket = ((red/64)*16) + ((green/64)*4) + ((blue/64));
                color_bucket[bucket] = color_bucket[bucket] + 1;
            }
        }
        
        for(int i=0; i<64; i++)
            color_bucket_percent[i] = (double)color_bucket[i] / (double)num_pixels;
        
        return color_bucket_percent;
    }
    
    public static double imageDistances(double[] a, double[] b)
    {
        double distance = 0;
        for(int i=0; i<64; i++)
            distance = distance + ( (a[i]-b[i])*(a[i]-b[i]) );
        distance = Math.sqrt(distance);
        return distance;
    }
}