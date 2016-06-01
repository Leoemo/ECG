package com.example.das.eeg;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
public class MainActivity extends Activity {
    double[] EEGdate = new double[10000];
    int j = 0;//录入文件数据到全局数组的时候控制数据个数
    int index = 0;//控制全局数组的角标
    Button button,button2;
    int constNum = 100;//控制整个屏幕的x数据宽度
    private Timer timer = new Timer();//计时器
    private GraphicalView chart;
    private TimerTask task;
    private double addY = -1;
    private long addX;
    private TimeSeries series;//单个线条的对象
    private XYMultipleSeriesDataset dataset;//多个线条组合的对象
    private Handler handler;//控制UI刷新
    Date[] xcache = new Date[constNum];//日期
    double[] ycache = new double[constNum];//坐标的缓存数组
    public String ReadSDFile(String filename){
        StringBuffer buffer = new StringBuffer();
        File file = new File(filename);
        try{
            FileInputStream fis = new FileInputStream(file);
            int  c;
            while((c =  fis.read())!=-1) {
                buffer.append((char)c);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(buffer);
    }//读取手机或者模拟器的数据文件
    public void Dateimport(String[] newarry){
        for(int i= 2;i<30000;i=i+3){
            EEGdate[j] = Double.parseDouble(newarry[i]);
            j++;
        }
        j = 0;
    }//将数据筛选出来赋值给全局数组
    public void restart(){
        setchart();
    }//重启线程函数（实际和setchart同样）
    private void updateChart() {
        //设定长度为20
        int length = series.getItemCount();
        if(length>=constNum) length = constNum;
        addY = 10*EEGdate[index++];
        addX = new Date().getTime();

        //将前面的点放入缓存
        for (int i = 0; i < length; i++) {
            xcache[i] =  new Date((long)series.getX(i));
            ycache[i] = series.getY(i);
        }

        series.clear();
        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        series.add(new Date(addX), addY);
        for (int k = 0; k < length; k++) {
            series.add(xcache[k], ycache[k]);
        }
        //在数据集中添加新的点集
        dataset.removeSeries(series);
        dataset.addSeries(series);
        //曲线更新
        chart.invalidate();
        if(index >= 10000 ){
            task.cancel();
        }//防止全局数组越界
    }//更新UI的相关线程
    public void  setchart(){
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //刷新图表
                updateChart();
                super.handleMessage(msg);
            }
        };
        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 200;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task,0,28);//刷新时间50ms

    }//创建相关的表
    private XYMultipleSeriesRenderer getDemoRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setChartTitle("心电信号图形化");//标题
        renderer.setChartTitleTextSize(20);
        renderer.setXTitle("时间");    //x轴说明
        renderer.setYTitle("心电信号");
        renderer.setAxisTitleTextSize(15);
        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsTextSize(15);    //数轴刻度字体大小
        renderer.setLabelsColor(Color.BLACK);
        renderer.setLegendTextSize(15);    //曲线说明
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        renderer.setShowLegend(true);
        renderer.setMargins(new int[] {5, 30, 15, 2});//上左下右{5, 30, 15, 2 })
        renderer.setMarginsColor(Color.WHITE);
        renderer.setPanEnabled(false,false);
        renderer.setShowGrid(false);
        renderer.setYAxisMax(40);//纵坐标最大值
        renderer.setYAxisMin(-20);//纵坐标最小值
        renderer.setInScroll(true);
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLUE);
        r.setChartValuesTextSize(15);
        r.setChartValuesSpacing(3);
        r.setPointStyle(PointStyle.POINT);
        r.setFillBelowLine(false);
        r.setFillBelowLineColor(Color.WHITE);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        return renderer;
    }//渲染器的初始化
    private XYMultipleSeriesDataset getDateDemoDataset() {
        dataset = new XYMultipleSeriesDataset();
        final int nr = 100;
        long value = new Date().getTime();
        series = new TimeSeries("ECG Series");
        for (int k = 0; k < nr; k++) {
//            series.add(new Date(value+k*10),EEGdate[index++]*10);
            series.add(new Date(value+k*10),0);

        }
        dataset.addSeries(series);
        return dataset;
    }//线条初始化
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String line = ReadSDFile("/mnt/sdcard/Download/nsrdb-16273.txt");//模拟器文件地址
//        String line = ReadSDFile("/storage/emulated/0/nsrdb-16273.txt");//手机文件地址
        String[] arry = line.split("\\s+");
        Dateimport(arry);
        button = (Button) findViewById(R.id.mtbyn);
        button2 = (Button) findViewById(R.id.mybtn2);
        LinearLayout layout1 = (LinearLayout)findViewById(R.id.line1);
        chart = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "mm:ss");
        layout1.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,800));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index =  0;
                restart();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.cancel();
            }
        });

    }//程序入口
    @Override
    public void onDestroy() {
        //当结束程序时关掉Timer
        timer.cancel();
        super.onDestroy();
    };//程序销毁时关闭计时器
}