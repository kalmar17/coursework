package coursework;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author User #1 threadNum(3)
 * java -jar omp4j-1.2.jar -d coursework -v Sample.java
 * java coursework.Sample
 *
 */
public class Sample {

    static int wX = 30;
    static int wT = 1000;

    static double A = 10;
    static double B = 5;
    static double a = 2;
    static double h = (double) 1/wX;
    static double tau = (double) Math.pow(h,2)*(0.1);
    static double[][] W = new double[wX][wT];

    static double func(double x, double t){
        return  Math.pow(Math.pow(x-A,2)/(6*a*(B-t)),2);
    }
    static double nextIter(int i,int k){
        return tau*a*(0.5*Math.pow(W[i][k],-0.5)*(Math.pow((W[i+1][k]-W[i-1][k])/(2*h),2))+
                Math.pow(W[i][k],0.5)*((W[i-1][k]-2*W[i][k]+W[i+1][k])/(Math.pow(h,2))))+W[i][k];
    }

    public static void main(String[] args) throws IOException {

        long startSerialTime = System.nanoTime();

        // omp parallel for
        for(int i=0;i<W.length;i++){
            W[i][0] = func(i*h,0);
        }

        // omp parallel for
        for(int i=0;i<W[0].length;i++){
            W[0][i] = func(0,i*tau);
            W[W.length-1][i] = func(1,i*tau);
        }

        for (int k = 0; k < W[0].length-1; k++){
            // omp parallel for
            for (int i = 1; i < W.length-1; i++){
                W[i][k+1] = nextIter(i,k);
            }

        }
        long endSerialTime = System.nanoTime();
        long timeSerialSpent = endSerialTime - startSerialTime;

        FileWriter file = new FileWriter("testOpenMP.txt");

        double AbsoluteAcc = 0;
        double Relative = 0;

        file.write("ListPlot3D[{"+"{"+(0.0)+","+(0.0)+","+W[0][0]+"}");

        for(int i=0;i<W.length;i++){
            for(int j=1;j<W[0].length;j++){
                file.write(",{"+(i*h)+","+(j*tau)+","+W[i][j]+"}");
                if(AbsoluteAcc < Math.abs(W[i][j]-func(i*h,j*tau))){
                    AbsoluteAcc = Math.abs(W[i][j]-func(i*h,j*tau));
                    Relative = AbsoluteAcc/W[i][j]*100;
                }
            }

        }
        file.write("}, Mesh -> All]");
        file.close();

        System.out.println( timeSerialSpent + " nano!");
        System.out.println("AbsoluteAcc = "+AbsoluteAcc);
        System.out.println("Relative = "+Relative);
    }
}