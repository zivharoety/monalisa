import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;

public class predict {

    public static void main(String[] args) throws IOException {

        String path = args[1];
        TreeNode root = deSerialize(args[0]);
        double [] [] confusionMatrix = new double [10][10];
        int [] count = new int [10];
        int totalcount = 0 ,succes = 0;
        LinkedList<Pic> pics = new LinkedList<Pic>();
        String sCurrentLine = "";
        BufferedReader reader = new BufferedReader(
                new FileReader((path)));
        while ((sCurrentLine = reader.readLine()) != null) {
            if(sCurrentLine.charAt(0)<'0' || sCurrentLine.charAt(0)>'9')
                sCurrentLine = reader.readLine();
            int label = (int) (sCurrentLine.charAt(0) - '0');
            String[] record = sCurrentLine.split(",");
            byte[] pix = new byte[784];
            for (int i = 1; i < record.length; i++) {
                pix[i - 1] = (byte) (Integer.parseInt(record[i]));
            }
            Pic pic = new Pic(pix, label);
            pics.add(pic);
            count[pic.getLabel()]++;
        }
        reader.close();
        Pic[] tests = pics.toArray(new Pic[pics.size()]);
        for (int i = 0; i < tests.length; i++) {
            Pic pp = tests[i];
            TreeNode tn = root;
            while (!tn.isLeaf()) {
                if (tn.getCond().check(pp))
                    tn = tn.getRight();
                else
                    tn = tn.getLeft();
            }
            totalcount++;
            if (tn.getLabel()==pp.getLabel())
                succes++;
            System.out.println(tn.getLabel());
        }
    }

    private static TreeNode deSerialize(String fileName) {
        TreeNode t;
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            t = (TreeNode) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("TreeNode class not found");
            c.printStackTrace();
            return null;
        }
        return t;
    }
}