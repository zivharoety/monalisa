import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


public class Main {

    public static void main(String [] args) throws IOException, InterruptedException {
        inputCheck(args);
        int version = Integer.parseInt(args[0]);
        int p = Integer.parseInt(args[1]); // percentage of validation labels
        int l = Integer.parseInt(args[2]); //max size to check
        List <Pic> pics = Utils.loadPics(args[3]);
        List<Pic> tests = new LinkedList<Pic>();
        List<Pic> trainPics = new LinkedList<Pic>();
        Utils.convertPics(tests,trainPics,pics,p);

        //_______Building Conditions________________________

        Condition [] conds;
        if (version==1) {		//version 1
            conds = new Condition[784];
            for(int i=0;i<28;i++){
                for (int j=0;j<28;j++){
                    final int x = i;
                    final int y = j;
                    conds[i*28 + j] = (Pic pic) -> pic.getPix(x,y) >= (byte)0;
                }
            }
        }
        else {		//version 2
            List<Condition> tmpConds = new LinkedList<Condition>();//in the end we will arrayify
            int [] count = Pic.count;
            double [][] rowsCount = Pic.rowsCount;
            double [][] colsCount = Pic.colsCount;
            int [] nonBlack = Pic.nonBlack;
            double [] avg = Pic.avg;
            double [] maxColInd = new double [10];
            for(int k=0;k<10;k++)
            {
                maxColInd[k] = (double)Pic.maxColIndPerLabel[k];
                maxColInd[k] /= count[k];
                for(int i=0;i<28;i++) {
                    rowsCount[i][k] /= count[k];
                    colsCount[i][k] /= count[k];
                }
                nonBlack[k]/=count[k];
                avg[k]/=count[k];
            }
            int nonBlackLowerBound = (int)(Utils.getMin(nonBlack)*0.8);
            int nonBlackUpperBound = (int)(Utils.getMax(nonBlack)*1.2);
            double avgLowerBound = Utils.getMin(avg)*0.8;
            double avgUpperBound = Utils.getMax(avg)*1.2;
            double avgGap = (avgUpperBound-avgLowerBound)/10;
            for(int i=0;i<10;i++) {
                final int x = i;
                tmpConds.add((Pic pic)->pic.getAvgShade()>=avgLowerBound+avgGap*x);
            }
            for(int i=nonBlackLowerBound;i<=nonBlackUpperBound;i+=3) {
                final int x = i;
                tmpConds.add((Pic pic)->pic.getNonBlackPix()>=x);
            }
            for(int i=0;i<28;i++) {
                final int y = i;
                for(int j=0;j<28;j++) {
                    final int x = j;
                    tmpConds.add((Condition)((Pic pic)->pic.getNumBlacksRow()[y]>x));
                    tmpConds.add((Condition)((Pic pic)->pic.getNumBlacksCol()[y]>x));
                }
            }
            for(int i=0;i<14;i++){
                for (int j = 0; j<14; j++){
                    final int x = i*2;
                    final int y = j*2;
                    tmpConds.add((Pic pic) -> pic.getPix(x,y) >= (byte)0);
                }
            }
            conds = (tmpConds.toArray(new Condition[tmpConds.size()]));
        }
        //___________________Making answers Table_____________________
        TreeNode.setConds(conds);
        TreeNode.prepChecksTable(pics);

        //___________________Building Learning Trees__________________


        PriorityQueue<TreeNode> leaves = new PriorityQueue<TreeNode>(new Comparator<TreeNode>() {
            @Override
            public int compare(TreeNode o1, TreeNode o2) {
                double sum = o1.getPossibleIG()*o1.getPics().size() - o2.getPossibleIG()*o2.getPics().size();
                if(sum < 0 )
                    return 1;
                else
                    return -1;
            }
        });
        double [] succesRates =new double[l+1];
        TreeNode root = new TreeNode(trainPics,0);
        root.calculateBestIG();
        leaves.add(root);
        int q = 1;
        for(int i=1;i<=Math.pow(2, l);i++){
            TreeNode curr = leaves.poll(); //taking the node with the best IG
            if(curr!=null && curr.split()) {
                curr.getLeft().calculateBestIG();
                curr.getRight().calculateBestIG();
                leaves.add(curr.getLeft());
                leaves.add(curr.getRight());
            }
            if(i==q) {
                double succes = tryOuts(tests, root);
                succesRates[(int)(Math.log(i)/Math.log(2))]=succes;
                q*=2;
            }
        }
        int t = (int)Math.pow(2,Utils.getMaxIndex(succesRates)); //choosing the optimal tree size

        // __________Building the optimal Decision tree ___________________

        PriorityQueue<TreeNode> leaves4Real = new PriorityQueue<TreeNode>(new Comparator<TreeNode>() {
            @Override
            public int compare(TreeNode o1, TreeNode o2) {
                double sum = o1.getPossibleIG()*o1.getPics().size() - o2.getPossibleIG()*o2.getPics().size();
                if(sum < 0 )
                    return 1;
                else
                    return -1;
            }
        });
        TreeNode root4Real = new TreeNode(pics,0);
        root4Real.calculateBestIG();
        leaves4Real.add(root4Real);
        for(int i=1;i<=t;i++){
            TreeNode curr = leaves4Real.poll();
            curr.split();
            curr.getLeft().calculateBestIG();
            curr.getRight().calculateBestIG();
            leaves4Real.add(curr.getLeft());
            leaves4Real.add(curr.getRight());
        }
        double succes4Real = tryOuts(pics, root4Real);
        int error = (int)(100 - succes4Real*100);

        Serialize(root4Real,args[4]);

        System.out.println("num: " + pics.size());
        System.out.println("error: " + error);
        System.out.println("size: " + t);



    }
    private static void inputCheck(String[] args) throws IOException {
        if(args.length<5)
            throw new IOException();
        int v = Integer.parseInt(args[0]);
        int p = Integer.parseInt(args[1]);
        int l = Integer.parseInt(args[2]);
        if ((v == 1 || v==2) & (p<=100 & p >= 0) & (l>=0))
            return;
        throw new IOException("One of the numerical integers is illegal");
    }
    public static void Serialize(TreeNode t, String fileName) {
        Utils.cleanTree(t);
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(t);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public static double tryOuts(List<Pic> tests, TreeNode root) {
        int [] [] mistakes = new int [10][10];
        int succes = 0;
        for(Pic pp: tests) {
            TreeNode tn = root;
            while(!tn.isLeaf()) {
                if(tn.getCond()==null)
                    System.out.println("CondNull");
                if(tn.getCond().check(pp))
                    tn = tn.getRight();
                else
                    tn = tn.getLeft();
            }
            if(pp.getLabel() == tn.getLabel()) {
                succes++;
            }
            else {
                mistakes[pp.getLabel()][tn.getLabel()]++;
            }
        }
        return (((double)succes/(double)tests.size()));
    }
}