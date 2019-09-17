package com.example.eventbus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * EventBus是Android和Java的发布/订阅事件总线
 * 事件总线是对发布-订阅模式的一种实现。
 * 它是一种集中事件处理机制，允许不同组件之间进行批次通信而又不需要相互依赖，达到解耦的目的。
 *
 * 常见的组件间通信方式
 *    1、intent
 *    2、Handler，主要是线程切换
 *    3、Interface
 *    4、Broadcast
 *    5、Aidl 进程通信
 *    6、Messenger 进程通信
 *
 * 用到WebView一般都是单开进程，避免内存泄漏带来的问题
 *
 * EventBus优点
 *         1、代码简单、快
 *         2、Jar包小，50k左右
 *         3、Activity，Fragment以及线程间通信优秀
 *         4、稳定，在1亿+应用中得到实践
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        startActivity(new Intent(this,SecondActivity.class));
    }

    // 需要对方法做一个标识，告诉EventBus只将带有该标识的方法放到EventBus
    @Subscrible(threadMode = ThreadMode.MAIN)
    public void getMessage(User user){
        Log.d("miracle", "getMessage: "+user.toString());
        Log.d("miracle", "getMessage: "+Thread.currentThread().getName());
    }

    @Subscrible(threadMode = ThreadMode.BACKGROUND)
    public void getMessage1(User user){
        Log.d("miracle", "getMessage1: "+user.toString());
        Log.d("miracle", "getMessage1: "+Thread.currentThread().getName());
    }


}
