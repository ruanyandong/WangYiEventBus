package com.example.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {

    private Map<Object, List<SubscribleMethod>> cacheMap;

    private static volatile EventBus instance;
    private Handler handler;
    private ExecutorService service;

    private EventBus(){
        cacheMap = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
        service = Executors.newSingleThreadExecutor();
    }

    public static EventBus getDefault(){
        if (instance == null){
            synchronized (EventBus.class){
                if (instance == null){
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    public void register(Object obj){
        // 就是寻找obj(本例子中对应的就是MainActivity)中所有的带有subscrible注解的方式 放到map中管理
        List<SubscribleMethod> list = cacheMap.get(obj);
        if (list == null){
            list = findSubscribleMethod(obj);
            cacheMap.put(obj,list);
        }
    }

    private List<SubscribleMethod> findSubscribleMethod(Object obj) {
        List<SubscribleMethod> list = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        while (clazz != null){

            // 凡是系统级别的父类，直接省略
            String name = clazz.getName();// 全路径名称
            if (name.startsWith("java.") ||
                    name.startsWith("javax.") ||
                    name.startsWith("android.") ||
                    name.startsWith("androidx.")){
                break;
            }
            // clazz.getMethods()会递归的获取本类以及父类（父类的父类......）的所有方法，一直到没有继承关系为止
            Method[] methods = clazz.getDeclaredMethods();//获取本类的所有方法
            for (Method method : methods) {
                Subscrible subscrible = method.getAnnotation(Subscrible.class);
                if (subscrible == null){
                    continue;
                }
                // 判断方法中的参数类型和个数
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1){
                    Log.e("Error", "EventBus only accept one parameter");
                }
                ThreadMode threadMode = subscrible.threadMode();
                SubscribleMethod subscribleMethod = new SubscribleMethod(method,threadMode,types[0]);
                list.add(subscribleMethod);
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    public void post(final Object type){
        // 直接循环cacheMap里的方法，找到对应的方法
        Set<Object> set = cacheMap.keySet();
        Iterator<Object> iterator = set.iterator();
        while (iterator.hasNext()){
            final Object obj = iterator.next();
            List<SubscribleMethod> list = cacheMap.get(obj);
            for (final SubscribleMethod subscribleMethod : list) {
                // 判断两个类之间的关系
                // a对象所对应的类是不是b对象所对应的类信息的父类或者接口
                if (subscribleMethod.getType().isAssignableFrom(type.getClass())){
                    switch (subscribleMethod.getThreadMode()){
                        case MAIN:
                            // 主-主
                            if (Looper.myLooper() == Looper.getMainLooper()){
                                invoke(subscribleMethod,obj,type);
                            }else {
                                // 子-主
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod,obj,type);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:
                            // 主-子
                            if (Looper.myLooper() == Looper.getMainLooper()){
                                service.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod,obj,type);
                                    }
                                });
                            }else {
                                // 子-子
                                invoke(subscribleMethod,obj,type);
                            }
                            break;
                    }

                }

            }
        }
    }

    private void invoke(SubscribleMethod subscribleMethod, Object obj, Object type) {
        Method method = subscribleMethod.getMethod();
        try {
            method.invoke(obj,type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


}
