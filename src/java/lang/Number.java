/*
 * Copyright (c) 1994, 2015, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 抽象类{@code Number}是平台的超类，这些类表示可转换为基本类型{@code byte},
 * {@code double}, {@code float}, {@code int}, {@code long} 和
 * {@code short} 的数值。
 *
 * 从特定的{@code Number}实现的数值转换到给定基本类型的转换的特定语义由{@code Number}
 * 实现定义。
 *
 * 对于平台类，转换通常类似于缩小基本类型转换或扩展基本类型转换
 * （如<cite>The Java&trade; Language Specification</cite>中所定义），
 * 用于基本类型之间进行转换。因此，转换可能会丢失有关数值总体大小信息，可能会
 * 丢失精度，甚至可能返回与输入不同的符号结果。
 *
 * 有关转换的详细信息，请参阅给定的{@code Number}实现的文档。
 *
 * @author      Lee Boynton
 * @author      Arthur van Hoff
 * @jls 5.1.2 Widening Primitive Conversions
 * @jls 5.1.3 Narrowing Primitive Conversions
 * @since   1.0
 */
public abstract class Number implements java.io.Serializable {
    /**
     * 返回指定数字的{@code int}值。
     *
     * @return  转换为 {@code int}类型后此对象表示的值。
     */
    public abstract int intValue();

    /**
     * 返回指定数字的{@code long}值。
     *
     * @return  转换为 {@code long}类型后此对象表示的值。
     */
    public abstract long longValue();

    /**
     * 返回指定数字的{@code float}值。
     *
     * @return  转换为 {@code float}类型后此对象表示的值。
     */
    public abstract float floatValue();

    /**
     * 返回指定数字的{@code double}值。
     *
     * @return  转换为 {@code double}类型后此对象表示的值。
     */
    public abstract double doubleValue();

    /**
     * 返回指定数字的{@code byte}值。
     *
     * <p>此实现将{@link #intValue} 强制转换的结果返回到 {@code byte}。
     *
     * @return  转换为 {@code byte}类型后此对象表示的值。
     *
     * @since   1.1
     */
    public byte byteValue() {
        return (byte)intValue();
    }

    /**
     * 返回指定数字的{@code short}值。
     *
     * <p>此实现将{@link #intValue} 强制转换的结果返回到 {@code short}。
     *
     * @return  转换为 {@code short}类型后此对象表示的值。
     *
     * @since   1.1
     */
    public short shortValue() {
        return (short)intValue();
    }

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = -8742448824652078965L;
}
