# UIPermission
使用注解方式对控件的显示和隐藏进行控制，或对点击事件的拦截

增加使用策略接口方式控制权限，触发方式：权限code变更时被动触发，某一个策略主动触发
TODO
1.ASM方式代码插桩替代反射方式拦截点击事件？？
2.控件除Visible权限Click权限，增加Enable权限
3.现在只支持在Activity中使用，对于ListView或RecycleView的Item还没有适配
4.增加Visible以及Enable的动画过渡