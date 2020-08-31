/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal;

import java.lang.annotation.*;

/**
 * {@code @HotSpotIntrinsicCandidate}注解特定于HotSpot虚拟机。它指示HotSpot VM可以（但不保证）
 * 引入带注解的方法。如果HotSpot VM使用手写的程序集或手写的编译器IR（编译器固有的）注解的方法以提高性能，
 * 则该方法会被激起。{@code @HotSpotIntrinsicCandidate}注解是Java库的内部注解，因此不应与应用程序
 * 代码有关联。
 *
 * Java库的维护者在修改使用{@code @HotSpotIntrinsicCandidate}注解标注的方法时必须考虑以下内容：
 *
 * <ul>
 *     <li>
 *         修改使用{@code @HotSpotIntrinsicCandidate}注解的方法时，必须更新HotSpot VM实现中的
 *         对应内部代码，以匹配带注解方法的语义。
 *     </li>
 *     <li>
 *         对于某些带注解的方法，如果使用Java字节码实现内部的方法，理所应当会执行这些检查，相对应的
 *         内部函数可能会省略一些低等级的检查。这是因为单个的Java字节码会隐式检查是否存在诸如{@code NullPointerException}
 *         和{@code ArrayStoreException}之类的异常。如果将这种方法替换为使用汇编语言编写的内部代码，
 *         那么必须在进入汇编代码之前，执行任何与常规字节码操作有关的所有检查。这些检查必须适当地针对内部
 *         函数的所有自变量，以及内部函数通过这些自变量获得的其他值（如果有的话）执行。可以通过检查该方法的非内部Java
 *         代码来推断，并确定代码可能引发哪些异常，包括未声明的隐式{@code runtimeException}s。
 *         因此，根据内部函数执行的数据访问，检查可能包括：
 *          <ul>
 *              <li>空引用(null reference)检查</li>
 *              <li>用作数组索引的基本类型数值的范围检查</li>
 *              <li>其他的对基本类型数值的有效性检查（比如，除零条件）</li>
 *              <li>数组中存储的引用类型的值检查</li>
 *              <li>对内部函数的索引数组进行数组长度检查</li>
 *              <li>引用类型转换（当形参是{@code Object}或其他一些弱类型时）</li>
 *          </ul>
 *     </li>
 *     <li>
 *         请注意，接收器值({@code this})作为附加参数传递给所有非静态方法。如果非静态方法是内部方法，
 *         则接收器值不需要进行空检查，但是（综上所述），必须检查内部函数从对象字段加载的任何值。为了清晰
 *         起见，最好使内部函数成为静态方法，从而使{@code this}的依赖项更加清晰。同样的，最好在进入内部
 *         方法前明确从对象字段中显示加载的所有必须值，并将这些值作为显示参数传递。首先，这对于空检查
 *         （或其他检查）可能是必须的。其次，如果内部函数从字段中重新加载值并不对其进行检查操作，则竞态
 *         条件可能能够将未检查的无效值引入内部函数。如果内部函数需要将值存储回对象字段，则应该从内部函数
 *         显式的返回该值；如果有多个返回值，编码人员应该考虑将它们缓冲在数组中。从内部函数中删除字段访问
 *         不仅阐明了JVM和JDK之间的接口；同样可以帮助HotSpot和JDK实现之间的解耦，因为如果JDK代码在内部
 *         函数之前和之后管理全部字段的访问，那么内部函数就可以被编码为与对象布局无关。
 *     </li>
 *     修改内部函数时，HotSpot VM的维护者必须考虑到以下内容。
 *     <ul>
 *         <li>
 *             新增一个内部函数时，请确保在Java库中相对应的方法都使用了{@code @HotSpotIntrinsicCandidate}
 *             进行注解标注，并且所有可能的调用序列都包含内部函数所省略的检查（如果有）。
 *         </li>
 *         <li>
 *             修改一个已经存在的内部函数时，必须更新Java库以匹配内在函数的语义，并执行内在函数所省略的所有检查（如果有）。
 *         </li>
 *     </ul>
 *  
 *     不直接参与维护Java库或HotSpot VM 的人员可以安全地忽略使用{@code @HotSpotIntrinsicCandidate}注解标注方法的事实。
 *
 *     HotSpot VM 定义（内部）内部函数的列表。在HotSpot VM支持的所有平台上，并非所有内部函数都可用。
 *     此外，一个给定的平台上的内部函数的可用性取决于HotSpot VM的配置（如，启动VM的标志集）。因此，
 *     使用{@code @HotSpotIntrinsicCandidate}标注方法并不能保证 该方法有HotSpot VM 内部函数。
 *
 *      如果启用了{@code CheckIntrinsics} VM标志，那么HotSpot VM 会检查（在加载类时）
 *      （1）VM的内部函数列表上的该类的所有方法都用{@code @HotSpotIntrinsicCandidate}进行注释
 *      （2）对于用{@code @HotSpotIntrinsicCandidate}标注的该类的所有方法，类别中都有一个内部函数。
 *
 * @since 9
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface HotSpotIntrinsicCandidate {
}
