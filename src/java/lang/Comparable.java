/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
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
import java.util.*;

/**
 * 此接口对实现它的每个类的对象强加了一个总的排序。这种排序被称为类的<i>自然排序</i>，
 * 类的{@code compateTo}方法被称为其<i>自然比较方法</i>。
 *
 * 实现此接口的list (和 array)对象可以通过{@link Collections#sort(List) Collections.sort}
 * (和 {@link Arrays#sort(Object[]) Arrays.sort})自动排序。实现此接口的对象，
 * 可以作为{@linkplain SortedMap sorted map}中的key，或 {@linkplain SortedSet sorted set}
 * 中的元素使用，而不需要指定{@linkplain Comparator comparator}。
 *
 * <p>类{@code C}的自然排序被称为<i>与equals一致</i>，当且仅当{@code C}类中的{@code e1}
 * 和{@code e2}在{@code e1.compareTo(e2) == 0}与{@code e1.equals(e2)}具有相同的布尔值。
 * 请注意，{@code null}不是任何类的实例，{@code e.compareTo(null)}应该抛出
 * {@code NullPointerException}，尽管{@code e.equals(null)}返回的是{@code false}。
 *
 * <p>强烈建议(尽管不是必要的)自然排序与equals一致。这是因为没有显式比较器的sorted sets
 * (和 sorted map)，当它们的自然排序与equals表现不一致时，在它们使用元素(或 keys) 时会表现得"奇怪"。
 * 特别是这样的sorted set(或 sorted map)违反了使用{@code equals}方法定义set(或 map)的一般约定。
 *
 * <p>例如，如果将两个key {@code a}和 {@code b} 添加到一个不使用显式比较器的sorted set，
 * {@code (!a.equals(b) 返回true) && (a.compareTo(b) == 0 返回false)}，则第二个 {@code add}
 * 操作返回 false(并且sorted set的大小不会增加)，因为从sorted set的角度看，{@code a} 和 {@code b}
 * 是相同的。
 *
 *<p>几乎所有实现了{@code Comparable}接口的Java核心类都具有与equals一直的自然排序。
 * 有一个特例是{@code java.math.BigDecimal}，其自然排序相当于相同值和不同精度的{@code BigDecimal}
 * 对象(例如 4.0 和 4.00)。（补充，当值相同，但是精度不同时compareTo方法返回0，而equals方法返回false，
 * 详见BigDecimal中的equals和compareTo方法的具体实现。）
 *
 * <p>对于数学上的倾向，定义给定 C 类的自然排序<i>关系</i>是：<pre>{@code
 *       {(x, y) such that x.compareTo(y) <= 0}.
 * }</pre>此总排序的<i>商</i>是:<pre>{@code
 *       {(x,y) such that x.compareTo(y) == 0}。
 * }</pre>
 *
 * For the mathematically inclined, the <i>relation</i> that defines
 * the natural ordering on a given class C is:<pre>{@code
 *       {(x, y) such that x.compareTo(y) <= 0}.
 * }</pre> The <i>quotient</i> for this total order is: <pre>{@code
 *       {(x, y) such that x.compareTo(y) == 0}.
 * }</pre>
 *
 * <p>根据{@code compareTo}的约定，商是一个在{@code C}上是<i>等价关系</i>，并且自然排序
 * 在{@code C}上是<i>总排序</i>。当我们说类的自然排序是<i>与equals一直</i>时，我们的意思
 * 是自然排序的商是由类的{@link Object#equals(Object) equals(Object)}方法定义的等价关系：
 * <pre>{(x, y) such that x.equals(y)}。</pre>
 *
 * <p>此接口是<a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 *    Java集合框架</a>的成员。
 *
 * @param <T> 该对象可以与之比较的对象的类型。
 *
 * @author  Josh Bloch
 * @see java.util.Comparator
 * @since 1.2
 */
public interface Comparable<T> {
    /**
     * 将此对象与指定的对象进行比较以获得排序。当此对象小于指定对象，返回一个负数，
     * 等于指定对象返回0，大于指定对象返回一个正数。
     *
     * <p>实现者必须确保对于所有的{@code x}和{@code y}，
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}，二者相等。
     * (这意味着 {@code y.compare(x)}抛出一个异常时，{@code x.compareTo(y)}必须抛出一个异常)。
     *
     * <p>实现这还必须确保该关系是可传递的：{@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)}
     * 意味着 {@code x.compareTo(z) > 0}。
     *
     * <p>最终，实现者必须确保{@code x.compareTo(y) == 0}意味着对于所有的{@code z},
     * {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}。
     *
     * <p>强烈建议，但<i>不</i>严格要求{@code (x.compareTo(y) == 0) == (x.equals(y))}。
     * 一般来说，任何实现{@code Comparable}接口且违反此条件的类，都应该清楚的表明这个事实。
     * 推荐的语言是"注意：此类具有与equals不一致的自然排序"
     * (Note: this class has a natural ordering that is inconsistent with equals.)。
     *
     * <p>在前面的描述中，符号{@code sgn(}<i>expression</i>{@code )}表示数学<i>signum</i>
     * 函数，其定义的返回{@code -1}, {@code 0}, {@code 1}，分别根据<i>expression</i>的值
     * 是否为负值，零，正值。
     *
     * @param   o 被比较的对象。
     * @return  一个负整数、零或正整数，因为该对象小于、等于或大于指定的对象。
     *
     * @throws NullPointerException 如果指定的对象为 null。
     * @throws ClassCastException 如果指定的对象的类型阻止它与本对象进行比较。
     *
     */
    public int compareTo(T o);
}
