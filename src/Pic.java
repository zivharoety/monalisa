import java.io.Serializable;

public class Pic implements Serializable {
	private static int counter = 0;
	public int id;
	private  byte [ ] pixels;
	private int label;
	private double AvgShade;
	private int [] numBlacksCol;
	private int [] numBlacksRow;
	private int diag1;
	private int diag2;
	public int maxColInd;
	static int [] count = new int [10];
	static int [] nonBlack = new int [10];// avg number of non-black pixels in the pics of each label
	static double [] avg = new double [10]; //avg shade of each label
	static double [] [] rowsCount =new double [28][10];
	//[i][j] is the avg number of non-black pixels for label j at the i-th row
	static double [] [] colsCount =new double [28][10];
	//[i][j] is the avg number of non-black pixels for label j at the i-th column
	static int [] maxColIndPerLabel = new int[10];


	public double getAvgShade() {
		return AvgShade;
	}

	public int getNonBlackPix() {
		return nonBlackPix;
	}


	private int nonBlackPix;

	public Pic(byte [] pix, int l){
		id = counter++;
		label = l;
		AvgShade = 0;
		nonBlackPix = 0;
		pixels = pix;
		numBlacksCol = new int[28];
		numBlacksRow = new int[28];
		for(int i=0;i<28;i++) {
			if(Utils.convertSigned(this.pixels[i*28 + i]) > 0)
				diag1++;
			if(Utils.convertSigned(this.pixels[i*28 + (27-i)]) > 0)
				diag2++;
			for(int j=0;j<28;j++) {
				AvgShade += Utils.convertSigned(pix[i*28+j]);
				if(pix[i*28+j]!=(byte)0) {
					nonBlackPix++;
					numBlacksCol[j]++;
					numBlacksRow[i]++;
				}
			}
			rowsCount[i][label] += numBlacksRow[i];//adding the num of nonblacks in row i to the general rowscounter
			colsCount[i][label] += numBlacksCol[i];//adding the num of nonblacks in col i to the general colscounter
		}
		AvgShade = AvgShade/784;
		this.maxColInd = Utils.getMaxIndex(numBlacksCol);
		maxColIndPerLabel[label] += maxColInd;
		count[label]++;
		nonBlack[label] += nonBlackPix;
		avg[label] += AvgShade;

	}
	public int[] getNumBlacksCol() {
		return numBlacksCol;
	}

	public int[] getNumBlacksRow() {
		return numBlacksRow;
	}

	public int getPix(int x, int y){
		byte b = this.pixels[y*28 + x];
		return b;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getDiag1() {
		return diag1;
	}
	public int getDiag2() {
		return diag2;
	}



}
