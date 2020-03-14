import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class TreeNode implements Serializable {

    private TreeNode left = null;
    private Semaphore leftLock;
    private TreeNode right = null;
    private Semaphore rightLock;
    private boolean isLeaf;
    private Condition cond; //if leaf condition = best possible condition.
    public int condInd = -1;
    private int probableLabel;
    private int[] labelsCount;
    private List<Pic> myPics;
    private double entropy;
    private double possibleIG = -1.0;
    private Semaphore lockME;
    public int depth;
    private static Condition[] conds;
    public static boolean[][] checks;


    public TreeNode(List<Pic> pics, int d) {
        lockME = new Semaphore(1);
        leftLock = new Semaphore(1);
        rightLock = new Semaphore(1);
        myPics = pics;
        labelsCount = new int[10];
        for (Pic p : pics) {
            labelsCount[p.getLabel()]++;
        }
        probableLabel = Utils.getMaxIndex(labelsCount);
        calculateEntropy();

        isLeaf = true;
        cond = null;
        depth = d;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void calculateBestIG() throws InterruptedException {
        int size = conds.length;
        int start1 = 0;
        int end1 = size / 5;
        int end2 = end1 +end1;
        int end3 = end2 + end1;
        int end4 = end3 + end1;

        Thread t1 = new Thread(() -> {
            try {
                calculateBestIGForThreads(start1, end1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            try {
                calculateBestIGForThreads(end1,end2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t2.start();
        Thread t3 = new Thread(() -> {
            try {
                calculateBestIGForThreads(end2,end3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t3.start();
        Thread t4 = new Thread(() -> {
            try {
                calculateBestIGForThreads(end3,end4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t4.start();
        Thread t5 = new Thread(() -> {
            try {
                calculateBestIGForThreads(end4,size);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t5.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
    }

    public void calculateBestIGForThreads(int start, int end) throws InterruptedException {
        for (int i = start; i < end; i++) {
            List<Pic> leftlist = new LinkedList<Pic>();
            List<Pic> rightlist = new LinkedList<Pic>();
            for (Pic p : myPics) {
                if (checks[i][p.id]) {
                    rightlist.add(p);
                } else {
                    leftlist.add(p);
                }
            }
            TreeNode PLeft = new TreeNode(leftlist, depth + 1);
            TreeNode PRight = new TreeNode(rightlist, depth + 1);
            if (leftlist.size() != 0 && rightlist.size() != 0) {
                double tempIG = calculateIG(PLeft, PRight);
                lockME.acquire();
                if (tempIG > possibleIG) {
                    possibleIG = tempIG;
                    this.condInd = i;
                    right = PRight;
                    left = PLeft;
                    //System.out.println("Changed best");
                }
                lockME.release();
            }

            rightLock.acquire();
            if (right == null)
                right = new TreeNode(new LinkedList<Pic>(), depth + 1);
            rightLock.release();

            leftLock.acquire();
            if (left == null)
                left = new TreeNode(new LinkedList<Pic>(), depth + 1);
            leftLock.release();
        }// end for each loop
    }
    public Boolean split() {
        if (this.myPics.size() < 2)
            return false;
        this.isLeaf = false;
        if (condInd == -1) {
            System.out.println("Say whaat");
        }
        this.myPics = null;
        this.cond = conds[condInd];
        return true;
    }

    public void calculateEntropy() {
        entropy = 0;
        if (myPics.size() == 0) {
            entropy = 1;
            return;
        }
        for (int i = 0; i < labelsCount.length; i++) {  //calculating the entropy
            if (labelsCount[i] > 0)
                entropy += (((double) labelsCount[i] / (double) myPics.size()) * (Utils.log2(((double) myPics.size() / (double) labelsCount[i]))));
        }
        return;

    }

    private double calculateIG(TreeNode left, TreeNode right) {
        double mutualEntropy = ((((double) left.myPics.size() * left.entropy) + ((double) right.myPics.size() * right.entropy)) /
                ((double) this.myPics.size()));
        if (this.entropy - mutualEntropy + 0.000001 < 0)
            System.out.println("WTF");
        return (this.entropy - mutualEntropy);
    }

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode getRight() {
        return right;
    }

    public double getPossibleIG() {
        return possibleIG;
    }

    public void setPics(List<Pic> pics) {
        this.myPics = pics;
    }

    public Condition getCond() {
        return cond;
    }

    public int getLabel() {
        return probableLabel;
    }

    public List<Pic> getPics() {
        return this.myPics;
    }

    public String getLabelsAsString() {
        String s = "" + labelsCount[0];
        for (int i = 1; i < labelsCount.length; i++) {
            s += ", " + labelsCount[i];
        }
        return s;
    }

    /*	public void calculateBestIGroot() {
            checks = new boolean [myPics.size()][conds.length];
            int i = 0;
            for(Pic p: myPics) {
                for(int j=0; j<conds.length; j++) {
                    checks[i][j] = conds[j].check(p);
                }
                i++;
            }
            calculateBestIG();
        }*/
    public static void setConds(Condition[] conditions) {
        conds = conditions;
    }

    public static void prepChecksTable(List<Pic> pics) {
        checks = new boolean[conds.length][pics.size()];
        int size = conds.length;
        int start1 = 0;
        int end1 = size / 3;
        int end2 = end1 + end1;
        Thread t1 = new Thread(() -> prepChecksTableForThreads(pics, start1, end1));
        t1.start();
        Thread t2 = new Thread(() -> prepChecksTableForThreads(pics, end1, end2));
        t2.start();
        Thread t3 = new Thread(() -> prepChecksTableForThreads(pics, end2, size));
        t3.start();

    }

    public static void prepChecksTableForThreads(List<Pic> pics, int start, int end) {
        //checks = new boolean [conds.length][pics.size()];
        for (Pic pic : pics) {
            for (int i = start; i < end; i++) {
                checks[i][pic.id] = conds[i].check(pic);
            }
        }
    }
}
