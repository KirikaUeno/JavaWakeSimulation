package simulation;

import company.Config;
import org.opensourcephysics.numerics.FFT;

import java.util.ArrayList;
import java.util.Arrays;

public class SpectrumHandling {
    private static double[] steps;
    private static double min;
    private static double max;

    public static double[] calculateSpectrum(ArrayList<Double> historySpaced){
        int size2 = historySpaced.size();
        int size = size2/2;

        FFT fourierAnalysis = new FFT();
        double[] fourier = new double[size2];
        double[] fourierRe = new double[size];
        double[] fourierIm = new double[size];
        double[] fourierAbs = new double[size];

        for (int k = 0; k < size2; k++) {
            fourier[k] = historySpaced.get(k);
        }
        for (int k = 0; k < size; k++) {
            fourier[2*k]*=Math.pow(Math.sin(k*Math.PI/(size-1)),2);
        }
        fourier = fourierAnalysis.transform(fourier);
        double max =  1;
        double wMax = 0;
        double maxR =  1;
        double wMaxR = 0;
        double maxI =  1;
        double wMaxI = 0;
        for (int k = 0; k < size2; k++) {
            if(k%2==0){
                fourierRe[k/2]=Math.abs(fourier[k]);
                if(fourierRe[k/2]>maxR){
                    maxR=fourierRe[k/2];
                    wMaxR=(k/2+1.0)/size;
                }
            } else {
                int n=(k-1)/2;
                fourierIm[n]=fourier[k];
                fourierAbs[n]=Math.sqrt(fourier[k]*fourier[k]+fourier[k-1]*fourier[k-1]);
                if(fourierIm[n]>maxI){
                    maxI=fourierIm[n];
                    wMaxI=(n+1.0)/size;
                }
                if(fourierAbs[n]>max){
                    max=fourierAbs[n];
                    wMax=(n+1.0)/size;
                }
            }
        }
        if(wMax>0.5) wMax=1-wMax;
        if(wMaxR>0.5) wMaxR=1-wMaxR;
        if(wMaxI>0.5) wMaxI=1-wMaxI;

        for (int k = 0; k < size; k++) {
            if(Config.fourierMode==1) fourierAbs[k]=Math.log(fourierAbs[k]/max);
            else fourierAbs[k]=fourierAbs[k]/max;
            if(Config.fourierMode==1) fourierRe[k]=Math.log(fourierRe[k]/maxR);
            else fourierRe[k]=fourierRe[k]/maxR;
            if(Config.fourierMode==1) fourierIm[k]=Math.log(fourierIm[k]/maxI);
            else fourierRe[k]=fourierIm[k]/maxI;
        }
        return fourierAbs;
    }

    public static double[] fitSpectrum(double[] rawSpectrum){
        double[] fourierAbsMean = rawSpectrum.clone();
        double minAbsMean = 0;
        int size = rawSpectrum.length;
        for (int k = 0; k < size; k++) {
            fourierAbsMean[k] = arrayPartAver(rawSpectrum,k-Config.step0/2,k+Config.step0/2-1);
            fourierAbsMean[k] = -5;
            minAbsMean=Math.min(minAbsMean,fourierAbsMean[k]);
        }
        double[] stepWeight = rawSpectrum.clone();
        double maxWeight = 0;
        /*for (int k = 0; k < size; k++) {
            stepWeight[k] = fourierAbsMean[k]-minAbsMean;
            maxWeight = Math.max(maxWeight,stepWeight[k]);
        }
        for (int k = 0; k < size; k++) {
            stepWeight[k] = (1-stepWeight[k]/maxWeight+lowerValue)/(1+lowerValue);
        }
        double step;
        steps = stepWeight.clone();
        for (int k = 0; k < size; k++) {
            step = step0*stepWeight[k]/2;
            /*if(step<1) fourierAbsMean[k] = fourierAbs[k];
            else fourierAbsMean[k] = arrayPartAver(fourierAbs,k-(int)step,k+(int)step-1);*/
        //    steps[k] = step;
        //}

        double[] fourierAbs1 = rawSpectrum.clone();
        double minFourier = 0;
        for (int k = 1; k < size-1; k++) {
            //fourierAbs1[k]=Math.pow(Math.E,Math.abs(fourierAbs[k]-fourierAbsMean[k]));
            //fourierAbs1[k]=fourierAbsMean[k]+Math.abs(fourierAbs[k]-fourierAbsMean[k]);
            fourierAbsMean[k] = -10;
            //if(steps[k]<1) fourierAbs1[k] = fourierAbs[k];
            //else fourierAbs1[k] = fourierAbsMean[k]+Math.abs(fourierAbs[k]-2*fourierAbsMean[k]+(fourierAbs[k-1]+fourierAbs[k+1])/2);
            fourierAbs1[k] = fourierAbsMean[k]+Math.abs(rawSpectrum[k]-2*fourierAbsMean[k]+(rawSpectrum[k-1]+rawSpectrum[k+1])/2);
            minFourier = Math.min(minFourier,fourierAbs1[k]);
        }
        for (int k = 0; k < size; k++) {
            stepWeight[k] = fourierAbs1[k]-minFourier;
            maxWeight = Math.max(maxWeight,stepWeight[k]);
        }
        for (int k = 0; k < size; k++) {
            stepWeight[k] = (1-stepWeight[k]/maxWeight+Config.lowerValue)/(1+Config.lowerValue);
        }
        double step;
        steps = stepWeight.clone();
        for (int k = 0; k < size; k++) {
            step = 40*stepWeight[k]/2;
            /*if(step<1) fourierAbsMean[k] = fourierAbs[k];
            else fourierAbsMean[k] = arrayPartAver(fourierAbs,k-(int)step,k+(int)step-1);*/
            steps[k] = step;
        }
        fourierAbs1 = rawSpectrum;
        return fourierAbs1;
    }

    public static double[] findFreq(double[] spectrum){
        spectrum = fitSpectrum(spectrum);
        ArrayList<Double> ans = new ArrayList<>();
        double step;
        max=0; min=0;
        for (int k = 1; k < spectrum.length/2; k++) {
            max=Math.max(max,spectrum[k]);
            min=Math.min(min,spectrum[k]);
        }
        for (int k = 1; k < spectrum.length/2; k++) {
            step = steps[k];
            if((k+0.0)/spectrum.length>Config.lowerBorder && (k+0.0)/spectrum.length <Config.upperBorder) {
                if ((spectrum[k] > spectrum[k - 1]) && (spectrum[k] > spectrum[k + 1])) {
                    if (step < 1) ans.add((k + 0.0) / spectrum.length);
                    else {
                        step = 1 + Math.sqrt((step - 1) * Config.stepGrowth);
                        if (isPointOutstanding(spectrum, k, k - (int) step, k + (int) step - 1)) {
                            ans.add((k + 0.0) / spectrum.length);
                            //System.out.println(step);
                        }
                    }
                }
            }
        }
        double[] ans1 = new double[ans.size()];
        for(int i = 0; i< ans.size(); i++){
            ans1[i]=ans.get(i);
        }
        System.out.println(Arrays.toString(ans1));
        return ans1;
    }

    public static void switchFourierMode(){
        if(Config.fourierMode==1) Config.fourierMode=0;
        else Config.fourierMode=1;
    }

    public static double arrayPartAver(double[] arr, int i, int f){
        double sum = 0;
        int count = f-i+1;
        int length = arr.length;
        for(int k = i; k< f+1; k++){
            if(k<length && k>-1) sum+=arr[k];
            else count--;
        }
        if(count>0) return sum/count;
        else return 0;
    }

    public static boolean isPointOutstanding(double[] arr, int p, int i, int f){
        double sum = 0;
        int count = f-i;
        int length = arr.length;
        for(int k = i; k< f; k++){
            if(k<(length-1) && k>-1) sum+=Math.abs(arr[k+1]-arr[k]);
            else count--;
        }
        double sumOfa = Math.abs(sum-Math.abs(arr[Math.min(f,length-1)]-arr[Math.max(i,0)]))/2;
        //double deviateP = arr[p]-Math.max(arr[p+1],arr[p-1]);
        double deviateP=0;
        double count1=4;
        for(int k = p-2; k< p+2; k++){
            if(k<(length-1) && k>-1) deviateP+=Math.abs(arr[k+1]-arr[k]);
            else count--;
        }
        deviateP /= count1;
        //System.out.println(p+" " +sumOfa+ " "+ sum/count + " " + deviateP + " " + count);
        return ((deviateP*(arr[p]-min)/(max-min)>(Config.threshold/*-Config.Zx/15*/)*sum/count) && arr[p]>-5/*+3*Math.sqrt(Config.intensity)*/);
    }

    private static double[] arrayMultiply(double[] arr, double a){
        double[] ans = new double[arr.length];
        for(int i = 0; i<arr.length;i++){
            ans[i]=arr[i]*a;
        }
        return ans;
    }

    private static double[] arraySum(double[] arr, double[] arr1){
        double[] ans = new double[arr.length];
        for(int i = 0; i<arr.length;i++){
            ans[i]=arr[i]+arr1[i];
        }
        return ans;
    }
}
