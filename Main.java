package coursework;

import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.IntStream;

public class Main {
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

        for(int i=0;i<W.length;i++){
            W[i][0] = func(i*h,0);
        }

        for(int i=0;i<W[0].length;i++){
            W[0][i] = func(0,i*tau);
            W[W.length-1][i] = func(1,i*tau);
        }

        for (int k = 0; k < W[0].length-1; k++){
             for (int i = 1; i < W.length-1; i++){
                W[i][k+1] = nextIter(i,k);
            }
        }
        long endSerialTime = System.nanoTime();
        long timeSerialSpent = endSerialTime - startSerialTime;

        FileWriter file = new FileWriter("testSerial.txt");

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

        long startTime = System.nanoTime();
        IntStream.range(0,W.length).parallel().forEach(i-> W[i][0] = func(i*h,0));

        IntStream.range(0,W[0].length).parallel().forEach(i->{
            W[0][i] = func(0,i*tau);
            W[W.length-1][i] = func(1,i*tau);
        });
        for (int k = 0; k < W[0].length-1; k++) {
            int finalK = k;
            IntStream.range(1, W.length - 1).parallel().forEach((i) -> W[i][finalK + 1] = nextIter(i, finalK));
        }
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        FileWriter file1 = new FileWriter("testStreamAPI.txt");

        double AbsoluteAccParall = 0;
        double RelativeParall = 0;

        file1.write("ListPlot3D[{"+"{"+(0.0)+","+(0.0)+","+W[0][0]+"}");

        for(int i=0;i<W.length;i++){
            for(int j=1;j<W[0].length;j++){
                file1.write(",{"+(i*h)+","+(j*tau)+","+W[i][j]+"}");
                if(AbsoluteAccParall < Math.abs(W[i][j]-func(i*h,j*tau))){
                    AbsoluteAccParall = Math.abs(W[i][j]-func(i*h,j*tau));
                    RelativeParall = AbsoluteAccParall/W[i][j]*100;
                }
            }

        }
        file1.write("}, Mesh -> All]");
        file1.close();

        System.out.println("1) программа выполнялась " + timeSerialSpent + " наносекунд");
        System.out.println("2) программа выполнялась " + totalTime + " наносекунд");
        System.out.println("AbsoluteAcc = "+ AbsoluteAcc);
        System.out.println("Relative = "+ Relative);
        System.out.println("AbsoluteAccParallel = "+ AbsoluteAccParall);
        System.out.println("RelativeParallel = "+ RelativeParall);
    }
}

