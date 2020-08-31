/*
 * Copyright (c) 1994, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import jdk.internal.HotSpotIntrinsicCandidate;

/**
 * {@code Object}类是类层次结构的根。
 * {@code Object}是所有类的超类。
 * 所有的对象，包括数组，都实现了此类的方法。
 *
 * @author  unascribed
 * @see     java.lang.Class
 * @since   1.0
 */
public class Object {

    /**
     * 本地方法在Java类中的定义是native进行修饰，且只有方法定义，没有方法实现。
     * Java有两种方法：Java方法和本地方法。
     *      1、Java方法是由Java语言编写，编译成字节码存储在class文件中。
     *      2、本地方法是由其他语言（比如C，C++，或者汇编）编写的，编译成和处理器相关的机器代码。
     * 本地方法保存在动态链接库中，格式是各个平台专有的。Java方法是平台无关的，但本地方法却不是。
     * 运行中的Java程序调用本地方法时，虚拟机装载包含这个本地方法的动态库，并调用这个方法。
     * 本地方法是联系Java程序和底层主机操作系统的连接方法。
     *
     * 由此可知，本地方法的实现是由其他语言编写并保存在动态链接库中，因此在Java类中不需要方法实现。
     * registerNatives本质上就是一个本地方法，但这又是一个有别于一般本地方法的本地方法，从方法名
     * 我们可以猜测方法应该是用来注册本地方法的。上述代码的功能就是先定义了registerNatives()方法，
     * 然后当该类被加载的时候，调用该方法完成对类中本地方法的注册。
     *
     * 1、到底注册了哪些方法？
     *      在Object类中，除了有registerNatives这个本地方法之外，还有hashCode()、clone()等本地
     *      方法， 而在Class类中有forName0()这样的本地方等等。也就是说，凡是包含registerNatives()
     *      本地方法的类，同时也包含了其他本地方法。所以，显然，当包含registerNatives()方法的类被加载
     *      的时候，注册的方法就是该类所包含的除了registerNatives()方法以外的所有本地方法。
     * 2、为什么要注册？
     *      在应用程序执行一个本地方法之前，它需要经过两个步骤来加载包含本地方法的本地库，然后连接到本地方法的实现：
     *          1. System.loadLibrary 查找并加载命名的本地库。例如，System.loadLibrary("foo")可能导致
     *             foo.dll在Win32上加载。
     *          2. 虚拟机在已加载的本地库之一中定位本地方法的实现。例如，对Foo.g本地方法的调用需要找到并链接本地函数
     *             Java_Foo_g，该函数可能会驻留在foo.dll中。
     *      JNI程序员无需依赖虚拟机在已加载的本地库中搜索本地方法，而是可以通过类引用，方法名称和方法描述符来注册一个
     *      函数指针来手动链接本地方法。
     *      registerNative()方法的作用就是取代第二步，让程序主动将本地方法链接到调用方，当Java程序需要调用本地方法
     *      时，就可以直接调用，而不需要虚拟机再去定位链接。使用registerNatives()方法的三点好处:
     *          1. 通过registerNatives方法在类被加载的时候就主动将本地方法链接到调用方，比当方法使用时再由虚拟机来定和链接更方便有效。
     *          2. 如果本地方法在程序中更新了，可以通过调用registerNatives方法进行更新。
     *          3. Java层序需要调动一个本地应用提供的方法时，因为虚拟机只会检索本地动态库，因而虚拟机是无法定位到本地方法实现的，
     *             这时候就只能使用registerNatives()方法进行主动链接。
     *          另外一个好处就是，通过registerNatives()方法，在定义本地方法实现的时候，可以不遵守JNI命名规范。所谓JNI命名规范，
     *          举个例子，Object中定义的本地方法registerNatives，那这个方法对应的本地方法名就叫Java_java_lang_Object_registerNatives，
     *          而在System类中定义的registerNatives方法对应的本地方法名叫Java_java_lang_System_registerNatives。也就是说，JNI命名规范
     *          要求本地方法名由"包名"+"方法名"构成，而上面的例子中，我们将Java中定义的方法名"g"和本地方法名"g_impl"链接了起来，这就是通过
     *          registerNatives方法的第四个好处。
     * 3、具体怎么注册？
     *      这个问题涉及到registerNatives()的底层C++源码实现，可以参考
     *       https://www.jianshu.com/p/f4b4b9006742 使用JNI_OnLoad动态注册函数
     *       https://hunterzhao.io/post/2018/04/06/hotspot-explore-register-natives/ JVM源码探秘】深入registerNatives()底层实现
     *
     * native方法在实现时名称都需要带上长长的一坨JNIEXPORT void JNICALL Java_HelloJNI_，
     * 包名很长的话更是不能忍。除此之外对于Native方法的方法名也不能修改。
     * 如果你想摆脱这种冗长的规则， 不妨尝试在JNI的JNI_OnLoad()方法中通过RegisterNatives注册Java需要调用的Native方法。
     * 注册navtive方法之前我们需要了解JavaVM,
     *  JNIEnv:
     *      JavaVM 和 JNIEnv 是JNI提供的结构体.
     *      JavaVM 提供了允许你创建和销毁JavaVM的"invokation interface"。理论上在每个进程中你可以穿件多个JavaVM，
     *      但是Android只允许创造一个。JNIEnv 提供了大部分JNI中的方法。在你的Native方法中的第一个参数就是JNIEnv.
     *      JNIEnv 用于线程内部存储。 因此， 不能多个线程共享一个JNIEnv. 在一段代码中如果无法获取JNIEnv，
     *      你可以通过共享JavaVM并调用GetEnv()方法获取。
     * JNI_OnLoad()方法：
     *      System.loadLibrary("NativeLib"); //NativeLib 为native模块名称
     *      Native 中的 JNI_OnLoad(JavaVM *vm, void *reserved) 方法会被调用。此时可以注册对应于Java层调用的navtive方法。
     * JNINativeMethod结构体：
     *      typedef struct {
     *          const char* name;
     *          const char* signature;
     *          void*       fnPtr;
     *      } JNINativeMethod;
     *      以上代码为jni.h中的源码， 可见JNINativeMethod包含三个元素： 方法名， 方法签名， native函数指针。
     *      该结构体用于描述需要注册的方法信息。
     * RegisterNatives方法：
     *      jint RegisterNatives(jclass clazz, const JNINativeMethod* methods, jint nMethods)
     *      该方法是JNI环境提供的用于注册Native方法的方法。
     */
    private static native void registerNatives();
    static {
        registerNatives();
    }

    /**
     * 构造一个新的Object对象。
     */
    @HotSpotIntrinsicCandidate
    public Object() {}

    /**
     * 返回此{@code Object}的运行时类。返回的{@code Class}对象是由所表示的类的
     * {@code static synchronized}方法锁定的对象。
     *
     * <p>
     *     <b>
     *      实际结果类型是{@code Class<? extend |X|>}，其中{@code |X|}是对调用了
     *      {@code getClass}的表达式的静态类型的擦除。
     *     </b>
     *     例如，次代码段中不需要强制转换：
     * 
     * <p>
     *     {@code Number n = 0;                             }<br>
     *     {@code Class<? extends Number> c = n.getClass(); }
     * 
     *
     * @return 表示该对象在运行时类的 {@code Class } 对象
     *
     * @jls 15.8.2 Class Literals
     */
    @HotSpotIntrinsicCandidate
    public final native Class<?> getClass();

    /**
     * 返回对象的哈希码.这个方法提供了散列表（hash tables）的效益，例如{@link java.util.HashMap}
     * 提供的散列表。
     * <p>
     *     {@code hasCode}的一般约定为：
     *     <ul>
     *         <li>
     *             只要在Java应用程序执行期间，同一个对象多次调用它，{@code hashCode}必须始终返回相同的
     *             整数，前提是不修改对象上的{@code equals}比较中使用的信息。从同一应用程序的一次执行到另一次
     *             执行。
     *         </li>
     *         <li>
     *              如果两个对象根据{@code equals(Object)}方法比较是相等的，那么在两个对象中的每个对象上调用
     *              {@code hashCode}方法必须产生相同的数值结果。
     *         </li>
     *         <li>
     *             并<em>不要求</em>根据 {@link java.lang.Object#equals(java.lang.Object)}
     *             方法不相等的两个对象，调用各自的 {@code hashCode} 方法必须返回不同的数值结果。然而，
     *             程序员应该意识到为不同对象产生不同的数值结果，可能会提高哈希表的性能。
     *         </li>
     *     </ul>
     * 
     * 在合理的范围内，由类{@code Object}定义的hashCode方法对于不同的对象返回不同的整数。
     * （在某个时间点，hashCode的实现可能会或可能不会是一个对象的内存地址）。
     *
     * @return  当前对象的哈希数值
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.lang.System#identityHashCode
     */
    @HotSpotIntrinsicCandidate
    public native int hashCode();

    /**
     * 表明其他某个对象是否"等于"该对象。
     * <p>
     *     {@code equals}方法在非null对象引用上实现等价关系：
     *     <ul>
     *         <li>
     *             <i>自反性：</i>对于任何非null的引用值 {@code x}, {@code x.equals(x)}应该返回{@code true}。
     *         </li>
     *         <li>
     *             <i>对称性：</i>对于任何的非null引用值{@code x}和{@code y}，{@code x.equals(y)}应该返回{@code true},
     *             当且仅当{@code y.equals(x)}返回{@code true}。
     *         </li>
     *         <li>
     *             <i>传递性：</i>对于任何的非null引用值{@code x}，{@code y}和{@code z}，
     *             如果{@code x.equals(y)}返回{@code true} 和 {@code y.equals(z)}返回{@code true}，
     *             那么{@code x.equals(z)}应当返回{@code true}。
     *         </li>
     *         <li>
     *             <i>一致性：</i>对于任何非null的引用值{@code x}和{@code y}，多次调用
     *             {@code x.equals(y)}始终返回{@code true}或始终返回{@code false}，
     *             前提是未修改对象上{@code equals}比较中使用的信息。
     *         </li>
     *         <li>
     *             对于任意的非null引用值{@code x}，{@code x.equals(null)}应该返回{@code false}
     *         </li>
     *     </ul>
     * 
     * <p>
     *      {@code Object}类的{@code equals}方法实现了对象上最有区别的可能等价关系；
     *      也就是说，对于任意的非null引用值{@code x}和{@code y}，当且仅当{@code x}和
     *      {@code y}引用的是同一个对象时，此方法返回{@code true}。
     * 
     * <p>
     *     注意，通常情况需要在重写此方法时覆盖{@code hashCode}方法，以便维护{@code hashCode}方法
     *     的常规协定，该方法声明相等的对象必须具有相等的哈希码。
     * 
     * <p>
     *
     * @param   obj   相比较的引用对象
     * @return  {@code true} 如果此对象与obj参数一致；
     *          {@code false} 其他.
     * @see     #hashCode()
     * @see     java.util.HashMap
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * 创建并返回此对象的一个复制。"复制"更精确的含义取决于对象的类别。一般意图是，
     * 对于任何对象{@code x}，表达式:
     * <blockquote>
     *     <pre>
     *         x.clone() != x
     *     </pre>
     * </blockquote>
     * 将会是{@code true}，对于表达式：
     * <blockquote>
     *     <pre>
     *         x.clone().getClass() == x.getClass()
     *     </pre>
     * </blockquote>
     * 将会是{@code true}，但是这些并不是绝对需要。
     * 通常情况是：
     * <blockquote>
     *     <pre>
     *         x.clone().equals(x)
     *     </pre>
     * </blockquote>
     * 将会是{@code true}，这不是一个绝对要求。
     * <p>
     *     按照惯例，返回的对象应该通过调用{@code super.clone}获得。如果一个类及其全部
     *     父类（除了{@code Object}）遵守这个惯例，那将会是{@code x.clone().getClass() == x.getClass()}。
     * 
     * <p>
     *     按照惯例，此方法返回的对象应该独立于此对象（正在被克隆的）。为了实现这种独立性，可能需要在返回前
     *     修改{@code super.clone}返回的对象的一个或多个字段。通常，这意味着复制包含被克隆对象的内部"深层结构"的任何可变对象，
     *     并使用对象副本的引用替换这些对象的引用。如果一个类只包含基本类型字段和对不可变的对象引用，那么通常情况下不需要修改{@code super.clone}
     *     返回对象中的任何字段。
     * 
     * <p>
     *     {@code Object}的{@code clone}方法执行特定的克隆操作。首先，如果此对象的类没有实现{@code Cloneable}接口，则抛出
     *     {@code CloneNotSupportedException}。注意，所有的数组都被视为实现了{@code Cloneable}接口，并且一个类型为{@code T[]}
     *     的数组的{@code clone}方法返回的类型是{@code T[]}，其中T是任意的基本类型或引用类型。
     *     否则此方法创建此对象的类的新实例，并使用该对象的相应的字段内容初始化其全部字段，就像是通过赋值一样；
     *     这些字段的内容本身不会被克隆。因此，该方法执行该对象的"浅拷贝"，不是一个"深拷贝"操作。
     * 
     * <p>
     *     {@code Object}自身并没有实现{@code Cloneable}接口，因此在类为{@code Object}的对象上调用{@code clone}方法
     *     将导致在运行时抛出异常。
     * 
     *
     * @return     此实例的克隆。
     * @throws  CloneNotSupportedException  如果这个对象的类不支持{@code Cloneable}接口。
     *               重写{@code clone}方法的子类也可以抛出此异常用于表明无法克隆实例。
     * @see java.lang.Cloneable
     */
    @HotSpotIntrinsicCandidate
    protected native Object clone() throws CloneNotSupportedException;

    /**
     * 返回对象的字符串表示形式。通常情况下，{@code toSting}方法返回"文本表示"此对象的字符串。结果应该是简洁但信息丰富的
     * 表示，便于人们的阅读。建议所有的子类重写这个方法。
     * <p>
     *     {@code Object}类的{@code toString}方法返回一个字符串，该字符串由对象为实例的类名称，符号字符'{@code @}'，
     *     以及对象的无符号十六进制表示组成。换句话说，这个方法返回值为
     *     <blockquote>
     *         <pre>
     *             getClass().getName() + '@' + Integer.toHaxString(hashCode())
     *         </pre>
     *     </blockquote>
     *     的字符串。
     * 
     *
     * @return  对象的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * 唤醒正在等待此对象的monitor的单个线程。如果有多个线程在等待此对象，则选择其中一个线程唤醒。
     * 选择是任意的，且由实现自行决定。线程通过其中一个{@code wait}方法的调用，等待对象的mointor。
     * <p>
     *     在当前线程放弃此对象上的锁之前，唤醒的线程将不能继续（执行）。唤醒的线程将以和通常一样的方式，与可能正在
     *     主动进行竞争的对象同步锁的其他线程进行竞争；例如，被唤醒的线程对于成为下一个锁定该对象的线程并没有特权或者劣势。
     * 
     * <p>
     *     这个方法只应该由拥有此对象的monitor的线程进行调用。线程以三种方式之一拥有对象的monitor：
     *     <ul>
     *         <li>
     *             通过执行该对象实例的同步方法。
     *         </li>
     *         <li>
     *             通过执行对象的同步{@code synchronized}声明的方法体。
     *         </li>
     *         <li>
     *             对于类型为@{@code Class}对象，该类执行同步静态方法。
     *         </li>
     *     </ul>
     * 
     * <p>
     *     一次仅能有一个线程拥有对象的monitor。
     * 
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的monitor的所有者。
     * @see        java.lang.Object#notifyAll()
     * @see        java.lang.Object#wait()
     */
    @HotSpotIntrinsicCandidate
    public final native void notify();

    /**
     * 唤醒等待此对象的monitor的所有线程。线程通过其中一个{@code wait}方法的调用，等待对象的mointor。
     * <p>
     *     在当前线程放弃此对象上的锁之前，唤醒的线程将不能继续（执行）。唤醒的线程将以和通常一样的方式，与可能正在
     *     主动进行竞争的对象同步锁的其他线程进行竞争；例如，被唤醒的线程对于成为下一个锁定该对象的线程并没有特权或者劣势。
     * 
     * <p>
     *     这个方法只应该由拥有此对象的monitor的线程进行调用。有关线程拥有monitor的方式说明，请参见{@code notify}方法。
     * 
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的monitor的所有者。
     * @see        java.lang.Object#notify()
     * @see        java.lang.Object#wait()
     */
    @HotSpotIntrinsicCandidate
    public final native void notifyAll();

    /**
     * 使当前线程等待，直到被唤醒，通常是通过<em>通知</em> 或 <em>中断</em>。
     * <p>
     *     在各个方面，此对象的行为就像是调用了{@code wait(0L, 0)}方法一样。
     *     请参阅{@link #wait(long, int)}方法的描述。
     * 
     *
     * @throws IllegalMonitorStateException 如果当前线程不是此对象的monitor的所有者。
     *
     * @throws InterruptedException 如果任何线程 在当前线程等待之前或当前线程正在等待中 中断当前线程。
     *         抛出此异常时，将清除当前线程的<em>中断状态</em>。
     * @see    #notify()
     * @see    #notifyAll()
     * @see    #wait(long)
     * @see    #wait(long, int)
     */
    public final void wait() throws InterruptedException {
        wait(0L);
    }

    /**
     * 使当前线程等待，直到被唤醒，通常是通过<em>通知</em> 或 <em>中断</em>，或直到经过一定的实时。
     * <p>
     *     在各个方面，此对象的行为就像是调用了{@code wait(timeoutMillis, 0)}方法一样。
     *     请参阅{@link #wait(long, int)}方法的描述。
     * 
     *
     * @param  timeoutMillis 等待的最长时间，以毫秒为单位
     * @throws IllegalArgumentException 如果{@code timeoutMillis}为负数
     * @throws IllegalMonitorStateException 如果当前线程不是此对象的monitor的所有者。
     * @throws InterruptedException 如果任何线程 在当前线程等待之前或当前线程正在等待中 中断当前线程。
     *                              抛出此异常时，将清除当前线程的<em>中断状态</em>。
     * @see    #notify()
     * @see    #notifyAll()
     * @see    #wait()
     * @see    #wait(long, int)
     */
    public final native void wait(long timeoutMillis) throws InterruptedException;

    /**
     * 使当前线程等待，直到被唤醒，通常是通过<em>通知</em> 或 <em>中断</em>，或直到经过一定的实时。
     * <p>
     *     当前线程必须拥有此对象的monitor锁。 请参阅{@link #notify() notify} 方法，用于描述线程可
     *     以成为monitor锁所有者的方式。
     * 
     * <p>
     *     此方法使当前线程（在这里称为<var>T</var>）将自身置于此对象的等待集合中，然后放弃此对象的任何
     *     和全部同步声明。注意，只放弃此对象上的锁；当前线程上可以同步的任何其他对象在等待时持有锁。
     * 
     * <p>
     *     为了线程调度目的，线程<var>T</var>将会被禁用并处于休眠直到有下列情况之一发生：
     *     <ul>
     *         <li>
     *             一些其他线程调用此对象的{@code notify}方法，并且线程<var>T</var>恰好被任意
     *             选择为要被唤醒的线程。
     *         </li>
     *         <li>
     *             一些其他线程调用此对象的{@code notifyAll}方法。
     *         </li>
     *         <li>
     *             一些其他线程{@linkplain Thread#interrupt() interrupts}线程<var>T</var>。
     *         </li>
     *         <li>
     *             指定的实时时间已经或多或少的过去了。实时时间(以纳秒为单位)由表达式{@code 1000000 * timeoutMillis + nanos}。
     *             如果{@code timeoutMillis} 和 {@code nanos}都是0，则不考虑实时，并且线程等待直到
     *             被其他原因之一唤醒。
     *         </li>
     *         <li>
     *             线程<var>T</var>被虚假地唤醒。（见下文。）
     *         </li>
     *     </ul>
     * 
     * <p>
     *     然后从该对象的等待集中删除线程<var>T</var>并重新启动线程调度。它以通常的方式与其他线程
     *     进行竞争，以便在对象上进行同步；一旦它重新获得对象的控制权，对线所有的同步声明都将恢复到
     *     原来的状态——即，调用{@code wait}方法时的状态。然后，线程<var>T</var>从{@code wait}
     *     方法的调用返回。因此，从{@code wait}返回时，对象和线程<var>T</var>的同步状态与调用{@code wait}
     *     方法时完全相同。
     * 
     * <p>
     *     线程可以在没有被通知，中断，或超时的情况下被唤醒，即所谓的<em>虚假唤醒</em>。虽然这在实际
     *     中很少发生，但应用程序必须通过测试应该导致线程被唤醒的条件来防范它，并且如果条件不满足则继续等待。
     *     请参阅下面的实例。
     * 
     * <p>
     *     有关该主题的更多信息，请参阅Brian Goetz和其他人的<em>实践中的Java并发</em>（Addison-Wesley, 2006）
     *     中的第 14.2 节"条件队列"或Joshua Bloch的<em>Effective Java，第二版</em>（Addison-Wesley, 2008）
     *     中的第69项。
     * 
     * <p>
     *     如果当前线程在等待之前或等待过程中被任何线程{@linkplain Thread#interrupt() interrupted}，
     *     则抛出{@code InterruptedException}。抛出异常时，将清除当前线程的<em>中断状态</em>。在如上所述
     *     恢复此对象的锁状态之前，不会抛出此异常。
     * 
     * @apiNote
     * 推荐的等待方法是检查 {@code while}循环中正在等待的条件，调用{@code wait}，如下例所示。除此之外，这种方法避免了可能
     * 由虚假唤醒引起的问题。
     *
     * <pre>
     *     {@code
     *          synchronized(obj) {
     *              while(<condition does not hold> and <timeout ont exceeded>){
     *                  long timeoutMillis = ...; //recompute timeout values
     *                  int nanos = ...;
     *                  obj.wait(timeoutMillis, nanos);
     *              }
     *              .. //Perform action appropriate to condition or timeout
     *          }
     *     }
     * </pre>
     *
     * @param  timeoutMillis 等待的最长时间（以毫秒为单位）
     * @param  nanos 额外时间，以纳秒为单位，范围为0-999999（含）
     *
     * @throws IllegalArgumentException 如果{@code timeoutMillis} 是负数，或{@code nanos}值超出范围
     * @throws IllegalMonitorStateException 如果当前线程不是对象的monitor的所有者
     * @throws InterruptedException 如果任何线程 在当前线程等待之前或当前线程正在等待中 中断当前线程。
     *                              抛出此异常时，将清除当前线程的<em>中断状态</em>。
     * @see    #notify()
     * @see    #notifyAll()
     * @see    #wait()
     * @see    #wait(long)
     */
    public final void wait(long timeoutMillis, int nanos) throws InterruptedException {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("timeoutMillis value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }

        //当nanos大于0时，（纳秒数大于0）那么超时时间（毫秒数）+1
        if (nanos > 0) {
            timeoutMillis++;
        }

        wait(timeoutMillis);
    }

    /**
     * 当垃圾收集确定没有对该对象的更多引用时，由对象上的垃圾收集器调用。子类重写{@code finalize}方法
     * 以处置系统资源或执行其他清理。
     * <p>
     *     {@code finalize}的一般约定是，当Java&trade;虚拟机确定不再有任何方法可以让尚未终止的线程访问
     *     该对象，则调用它，除非是由于某个其他对象或类的终结所采取的的操作，而该对象或类已经准备好finalized。
     *     {@code finalize}方法可以采取任何操作，包括可以使此对象再次可用于其他线程；然而，{@code finalize}
     *     通常的目的是在对象被不可撤销的丢弃之前执行清理操作。例如，表示输入/输出连接的对象的finalize方法可能会
     *     执行显式I/O事务，以在永久废弃对象之前断开连接。
     * 
     *
     * <p>
     *     {@code Object}类的{@code finalize}方法不执行特殊操作；它只是正常返回。{@code Object}的子类可以
     *     重写此定义。
     * 
     *
     * <p>
     *     Java编程语言不再保证哪个线程将会任何给定对象调用{@code finalize}方法。但是，可以保证，调用finalize时，
     *     调用finalize的线程不会持有任何用户可见的同步锁。如果finalize方法抛出未捕获的异常，该异常将会被忽略，且
     *     对象终止finalization。
     * 
     * 
     * <p>
     *     在为对象调用{@code finalize}方法之后，不会采取进一步操作，直到Java虚拟机再次确定不再有任何方法可以通过任
     *     何未死亡的线程访问此对象之前，包括其他对象或类准备完成的可能的操作，此时可以丢弃该对象。
     * 
     *
     * <p>
     *     对于任何给定的对象，Java虚拟机永远不会多次调用{@code finalize}方法。
     * 
     *
     * <p>
     *     {@code finalize}方法抛出的任何异常都会导致该对象finalization终止，但会被忽略。
     * 
     *
     * @apiNote
     * 嵌入非堆资源的类有很多选项可以用于清理这些资源。该类必须确保每个实例的生命周期长于其嵌入的任何资源的声明周期。
     * {@link java.lang.ref.Reference#reachabilityFence} 可用于确保在对象中嵌入的资源正在使用时对对象保持可达状态。
     *
     * <p>
     *     除非子类的实例在被回收之前必须清理嵌入非堆资源，否则子类应该避免重写{@code finalize}方法。
     *     与构造函数不同，Finalizer调用不会自动链接。如果子类重写了{@code finalize}则必须显示调用超类finalizer。
     *     为了防止过早终止finalize链的异常，子类应该使用{@code try-finally}块类确保始终会调用{@code super.finalize()}。
     *     例如:
     *     <pre>
     *         {@code @Override
     *                protected void finalize() throws Throwable {
     *                    try{
     *                        ... //cleanup subclass state
     *                    } finally {
     *                        super.finalize();
     *                    }
     *                }
     *         }
     *     </pre>
     * 
     *
     * @deprecated finalization机制本质上存在问题。Finalization可能会导致性能问题，死锁，以及挂起。
     * finalizer中的错误会导致资源泄露；如果不再需要，则无法取消finalization；并且在不同对象的{@code finalize}
     * 方法的调用中没有指定顺序。此外，无法保证finalization的确定时间。{@code finalize}方法只有在不确定的延迟之后
     * （如果有的话）才可以在可终结对象上调用。
     * 实例持有非堆资源的类应当提供一种方法，以实现显示释放那些资源，并且它们还应该实现{@link AutoCloseable}（如果适用）。
     * {@link java.lang.ref.Cleaner} 和 {@link java.lang.ref.PhantomReference}
     * The {@link java.lang.ref.Cleaner} and {@link java.lang.ref.PhantomReference}提供了一种更加灵活和有效
     * 的方法在对象不可达时释放资源。
     *
     * @throws Throwable 此方法引发的{@code Exception}
     * @see java.lang.ref.WeakReference
     * @see java.lang.ref.PhantomReference
     * @jls 12.6 Finalization of Class Instances
     */
    @Deprecated(since="9")
    protected void finalize() throws Throwable { }
    
}
