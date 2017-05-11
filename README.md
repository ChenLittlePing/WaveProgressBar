# WaveProgressBar
A round loading view with dynamic wave
<br> 一个使用Path的二阶贝塞尔曲线画出波浪，并实现动态滚动的Loading控件

![image](https://github.com/ChenLittlePing/WaveProgressBar/blob/master/gif/demo.gif)

## How to use
#### In XML
```xml
    <com.waveprogressbar.WaveProgressBar
        android:id="@+id/wave"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_centerInParent="true"
        app:fillColor="@android:color/holo_red_light"
        app:waveColor="@android:color/holo_green_light"
        app:strokeColor="@android:color/white"/>
```

#### In Activity
```java
    mWave = (WaveProgressBar) findViewById(R.id.wave);
    mWave.setProgress(50);
```
