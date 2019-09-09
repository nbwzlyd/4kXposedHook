package com.lyd.a4kxposed;

import android.content.Context;
import android.text.TextUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Field;

public class FourK_Xposed implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        XposedBridge.log("FourK_Xposed 模块生效");
        //先判断是不是我们想要hook的应用，以包名判断
        if (!TextUtils.equals(lpparam.packageName,"com.evo.watchbar.tv")){
            return;
        }

        //腾讯加固，需要获取对应classloader
        XposedHelpers.findAndHookMethod("com.tencent.StubShell.TxAppEntry", lpparam.classLoader,
                "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        //获取到Context对象，通过这个对象来获取classloader
                        Context context = (Context) param.args[0];
                        //获取classloader，之后hook加固后的就使用这个classloader
                        hookLogin(context.getClassLoader());
                        hookVip(context.getClassLoader());
                        hookURL(context.getClassLoader());
                    }

                });
    }

    private void hookURL(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(classLoader.loadClass("com.evo.watchbar.tv.bean.RealUrlBean$Data"), "getRetCode", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(0);
                }
            });
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void hookVip(ClassLoader classLoader)  {
        try {
            XposedHelpers.findAndHookMethod(classLoader.loadClass("com.evo.watchbar.tv.utils.UserUtils"), "checkVIP", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(true);

                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    private static void hookLogin(ClassLoader classLoader){
        Class cls = null;
        try {
            cls = classLoader.loadClass("com.evo.watchbar.tv.storage.MyStorage");
            Field field = cls.getField("user");
            Class User = classLoader.loadClass("com.evo.m_base.bean.User");
            field.set(cls.newInstance(),User.newInstance());
        } catch (Exception e) {
            XposedBridge.log(e.getMessage());

        }

    }
}
