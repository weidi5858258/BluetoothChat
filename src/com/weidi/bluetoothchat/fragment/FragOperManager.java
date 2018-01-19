package com.weidi.bluetoothchat.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.weidi.eventbus.EventBus;
import com.weidi.eventbus.EventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.weidi.bluetoothchat.Constant.HIDE;
import static com.weidi.bluetoothchat.Constant.POPBACKSTACK;

/**
 * fragment在add,replace,hide,show时会调用哪些生命周期方法
 * <p>
 * class desc: Fragment操作类
 * 替换时删除id相同的fragment然后添加，只有一层，添加是多层
 * 对于fragment的使用基本有两种，
 * 一种是add方式后再进行show或者hide，这种方式切换fragment时不会让fragment重新刷新，
 * 而用replace方式会使fragment重新刷新，因为add方式是将fragment隐藏了而不是销毁再创建，
 * replace方式每次都是重新创建。
 */

/**
 * Fragment操作类
 * 1、有时候，我们需要在多个Fragment间切换，
 * 并且保存每个Fragment的状态。官方的方法是使用
 * replace()来替换Fragment，但是replace()的调用
 * 会导致Fragment的onCreteView()被调用，所以切换
 * 界面时会无法保存当前的状态。因此一般采用add()、hide()与show()配合，
 * 来达到保存Fragment的状态。
 * 2、第二个问题的出现正是因为使用了Fragment的状态保存，当系统内存不足，
 * Fragment的宿主Activity回收的时候，
 * Fragment的实例并没有随之被回收。
 * Activity被系统回收时，会主动调用onSaveInstance()
 * 方法来保存视图层（View Hierarchy），
 * 所以当Activity通过导航再次被重建时，
 * 之前被实例化过的Fragment依然会出现在Activity中，
 * 然而从上述代码中可以明显看出，再次重建了新的Fragment，
 * 综上这些因素导致了多个Fragment重叠在一起。
 * <p>
 * 在onSaveInstance()里面去remove()所有非空的Fragment，然后在onRestoreInstanceState()
 * 中去再次按照问题一的方式创建Activity。当我处于打开“不保留活动”的时候，效果非常令人满意，然而当我关闭“不保留活动”的时候，问题却出现了。当转跳到其他Activity
 * 、打开多任务窗口、使用Home回到主屏幕再返回时，发现根本没有Fragment了，一篇空白。
 * <p>
 * 于是跟踪下去，我调查了onSaveInstanceState()与onRestoreInstanceState()
 * 这两个方法。原本以为只有在系统因为内存回收Activity时才会调用的onSaveInstanceState()
 * ，居然在转跳到其他Activity、打开多任务窗口、使用Home回到主屏幕这些操作中也被调用，然而onRestoreInstanceState()
 * 并没有在再次回到Activity时被调用。而且我在onResume()发现之前的Fragment只是被移除，并不是空，所以就算你在onResume()
 * 中执行问题一中创建的Fragment的方法，同样无济于事。所以通过remove()宣告失败。
 * <p>
 * 接着通过调查资料发现Activity中的onSaveInstanceState()里面有一句super.onRestoreInstanceState(savedInstanceState)
 * ，Google对于这句话的解释是“Always call the superclass so it can save the view hierarchy
 * state”，大概意思是“总是执行这句代码来调用父类去保存视图层的状态”。其实到这里大家也就明白了，就是因为这句话导致了重影的出现，于是我删除了这句话，然后onCreate()
 * 与onRestoreInstanceState()中同时使用问题一中的创建Fragment方法，然后再通过保存切换的状态，发现结果非常完美。
 * <p>
 * 只能在v4包中才能使用
 * fTransaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
 */
public class FragOperManager implements Serializable, EventListener {

    private static final String TAG = "FragOperManager";
    /**
     * FragmentActivity 实例
     */
    //    private Activity context;
    private Activity activity;

    /**
     * BaseFragment 管理器
     */
    private FragmentManager fManager;

    /**
     * 装Fragment的容器
     */
    private int containerId;

    /**
     * 该Activity所有fragment的集合
     */
    private List<Fragment> fragmentsList;

    /**
     * @param activity    FragmentActivity 实例
     * @param containerId 容器Id
     */
    public FragOperManager(Activity activity, int containerId) {
        if (activity == null) {
            throw new NullPointerException("");
        }
        if (containerId <= 0) {
            throw new IllegalArgumentException("");
        }
        this.activity = activity;
        this.containerId = containerId;
        fManager = activity.getFragmentManager();
        fragmentsList = new ArrayList<Fragment>();
        EventBus.getDefault().register(this);
    }

    /**
     * @param fragment
     * @param tag
     */
    public void enter(Fragment fragment, String tag) {
        if (fragment == null) {
            throw new NullPointerException("要进入的fragment不能为null.");
        }

        FragmentTransaction fTransaction = fManager.beginTransaction();
        // 保证fragment在最后一个
        if (!fragmentsList.contains(fragment)) {
            fragmentsList.add(fragment);
            fTransaction.add(containerId, fragment, tag);
            fTransaction.addToBackStack(tag);
        } else {
            fragmentsList.remove(fragment);
            fragmentsList.add(fragment);
        }

        int count = fragmentsList.size();
        for (int i = 0; i < count - 1; i++) {
            Fragment hideFragment = fragmentsList.get(i);
            // fragment隐藏时的动画

            // fTransaction.setCustomAnimations(R.anim.push_right_in, R.anim.push_left_out2);
            fTransaction.hide(hideFragment);
        }
        // fragment显示时的动画
        // fTransaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        fTransaction.show(fragment);
        // 旋转屏幕,然后去添加一个Fragment,出现异常
        // 旋转屏幕后
        // java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        fTransaction.commit();
    }

    private void exit(Fragment fragment, int exitType) {
        if (fragment == null) {
            throw new NullPointerException("要进入的fragment不能为null.");
        }
        if (!fragmentsList.contains(fragment)) {
            return;
        }
        FragmentTransaction fTransaction = fManager.beginTransaction();
        switch (exitType) {
            case HIDE:
                fTransaction.hide(fragment);
                fragmentsList.remove(fragment);
                Fragment showFragment = fragmentsList.get(fragmentsList.size() - 1);
                fTransaction.show(showFragment);
                fragmentsList.add(0, fragment);
                break;
            case POPBACKSTACK:
                fManager.popBackStack();
                fragmentsList.remove(fragment);
                int count = fragmentsList.size();
                for (int i = 0; i < count; i++) {
                    Fragment hideFragment = fragmentsList.get(i);
                    fTransaction.hide(hideFragment);
                }
                showFragment = fragmentsList.get(count - 1);
                fTransaction.show(showFragment);
                break;
            default:
        }
        fTransaction.commit();
    }

    public List<Fragment> getFragmentsList() {
        return fragmentsList;
    }

    /**
     * 不需要调用
     */
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public String toString() {
        return "FragOperManager{" +
                "activity=" + activity +
                ", fManager=" + fManager +
                ", containerId=" + containerId +
                ", fragmentsList=" + fragmentsList +
                '}';
    }

    /**
     * 退出Fragment时打算隐藏那么就传HIDE;
     * 如果退出Fragment时弹出后退栈那么就传POPBACKSTACK.
     * 如果退出时是隐藏的,那么在进入这个Fragment时它的对象不能每次都new,只能new一次
     *
     * @param what
     * @param object
     */
    @Override
    public void onEvent(int what, Object object) {
        switch (what) {
            case HIDE:
                exit((Fragment) object, HIDE);
                break;
            case POPBACKSTACK:
                exit((Fragment) object, POPBACKSTACK);
                break;
            default:
        }
    }

}
