import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Utils {

    public static LinkedList<Pic> loadPics(String path) throws IOException {
        LinkedList<Pic> pics = new LinkedList<Pic>();
        String sCurrentLine = "";
        BufferedReader reader = new BufferedReader(
                new FileReader((path)));
        while ((sCurrentLine = reader.readLine()) != null) {
            if(sCurrentLine.charAt(0) < '0' || sCurrentLine.charAt(0) > '9'){
                sCurrentLine = reader.readLine();
            }
            int label = (int)(sCurrentLine.charAt(0)-'0');
            String [] record = sCurrentLine.split(",");
            byte [] pix = new byte[784];
            for (int i = 1; i < record.length; i++) {
                pix[i-1] = (byte)(Integer.parseInt(record[i]));
            }

            Pic pic = new Pic(pix, label);
            pics.add(pic);
        }
        reader.close();
        return pics;
    }
    public static int getMaxIndex(int[] array){
        int max = array[0];
        int maxIndex = 0;
        for (int i=1;i<array.length;i++){
            if (max<array[i]){
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    public static int getMaxIndex(double[] array){
        double max = array[0];
        int maxIndex = 0;
        for (int i=1;i<array.length;i++){
            if (max<array[i]){
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    public static List<Pic> arrayToList (Pic [] pics, int sampleSize){
        List<Pic> myPics = new LinkedList<Pic>();
        for(int i=0;i<sampleSize;i++){
            myPics.add(pics[i]);
        }
        return myPics;
    }
    public static double log2(double x)
    {
        return  (Math.log(x) / Math.log(2));
    }
    public  static int getMax(int [] x){
        int max = x[0];
        for(int t: x) {
            if(t>max)
                max = t;
        }
        return max;
    }
    public  static double getMax(double [] x){
        double max = x[0];
        for(double t: x) {
            if(t>max)
                max = t;
        }
        return max;
    }
    public  static int  getMin(int [] x){
        int max = x[0];
        for(int t: x) {
            if(t<max)
                max = t;
        }
        return max;
    }
    public  static double  getMin(double [] x){
        double max = x[0];
        for(double t: x) {
            if(t<max)
                max = t;
        }
        return max;
    }
    public static int convertSigned(byte b) {
        if (b < 0)
            return 256 + b;
        else
            return b;
    }
    public static void convertPics(List<Pic> tests, List<Pic> trainPics, List<Pic> picsList, int p) {
        int end = picsList.size();
        int needed = end*p/100;
        Random rng = new Random();
        int remaining = end;
        Iterator<Pic> iter = picsList.iterator();
        for (int i = 0; i < end && needed > 0; i++) {
            Pic pic = iter.next();
            double probability = rng.nextDouble();
            if (probability < ((double) needed) / (double) remaining) {
                needed--;
                tests.add(pic);
            }
            else {
                trainPics.add(pic);
            }
            remaining--;
        }
        while(iter.hasNext()){
            trainPics.add(iter.next());
        }
    }
    public static void cleanTree(TreeNode t) {
        if(t.isLeaf())
            t.setPics(null);
        else
        {
            cleanTree(t.getLeft());
            cleanTree(t.getRight());
        }
    }
}
