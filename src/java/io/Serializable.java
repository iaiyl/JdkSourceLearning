/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 通过实现 java.io.Serializable 接口可以启用类的可序列化。
 *
 * <p><strong>警告：对不信任的数据进行反序列化本质上是危险的，应当避免。
 * 应当根据{@extLink secure_coding_guidelines_javase Java SE 安全编码准则}的"序列化和反序列化"
 * 部分仔细校验不信任数据。{@extLink serialization_filter_guide 序列化过滤}描述了防御性使用串行
 * 过滤器的最佳实践。
 * </strong></p>
 * 
 * 未实现此接口的类将不会将其任何状态序列化或反序列化。 可序列化类的所有子类型本身都是可序列化的。
 * 序列化接口没有方法或字段，仅用于标识可序列化的语义。<p>
 *
 * 为了允许序列化非序列化类的子类型，子类可以承担保存和恢复超类的public , protected, 以及（如果可访问）
 * package 字段的状态的责任。只有当它扩展的类具有可访问的无参构造函数来初始化类的状态时，子类才可以承担此责任。
 * 如果不是这种情况，则声明类Serializable是错误的。将在运行时检测到错误。<p>
 *
 * 反序列化期间，非可序列化的类的字段将使用 public 或 protected 无参构造函数进行初始化。
 * 必须可以对可序列化的子类访问无参构造函数。可序列化子类的字段将从流中恢复。<p>
 *
 * 当遍历图时，可能会遇到不支持Serializable接口的对象。在这种情况下，将会抛出NotSerializableException，
 * 并将其标识非可序列化对象的类。<p>
 *
 * 在序列化和反序列化的过程中，需要特殊处理的类必须使用这些精确签名实现特殊方法：
 * <pre>
 *     private void writeObject(java.io.ObjectOutputStream out)
 *          throws IOException;
 *     private void readObject(java.io.ObjectInputStream in)
 *          throws IOException, ClassNotFoundException;
 *     private void readObjectNoData()
 *          throws ObjectStreamException;
 * </pre>
 *
 * <p> writeObject方法负责为其特定类编写对象的状态，以便相应的readObject方法可以恢复它。
 * 可以通过调用out.defaultWriteObject来调用保存对象的字段的默认机制。该方法不需要关注属
 * 于其超类或子类的状态。通过使用writeObject方法或使用DataOutput支持的基本数据类型的方法
 * 将各个字段写入ObjectOutputStream来保存状态。
 *
 * <p>readObject方法负责从流中读取并恢复类的字段。它可以调用 in.defaultReadObject来调用
 * 恢复对象的非静态字段和非瞬态字段的默认机制。defaultReadObject方法使用流中的信息将流中保存
 * 的对象的字段以及当前对象中相应命名的字段分配。这可以处理当类已发展为添加新字段时的情况。该方法
 * 不需要关注属于其超类或子类的状态。通过从ObjectInputStream读取各个字段的数据并对对象的相应
 * 字段进行赋值来恢复状态。DataInput支持读取基本数据类型。
 *
 * <p>如果序列化流没有将给定类列出为被反序列化的对象的超类，readobjectnodata方法负责为其特定
 * 的类初始化对象的状态。（补充：对于可序列化的对象，在对子类实例进行反序列化并且序列化流未将所涉
 * 及的类作为反序列化对象的超类列出的情况下，readObjectNoData方法允许类控制其自身字段的初始化。）
 * 如果接收方使用与发送方不同版本的反序列化实例的类，并且接收方版本扩展了发送方的版本未扩展的类。
 * 则可能发生这种情况。如果序列化流已经被篡改，也可能发生这种情况；因此，尽管存在"恶意"或不完整的源流，
 * readObjectNoData对于正确初始化反序列化对象非常有用。
 *
 * <blockquote>
 *     Student extends Person，如果序列化类Student（那时候还没有继承Person）时，没有Person
 *     的信息，此时序列化会调用readobjectnodata方法，为Person中的字段进行默认赋值。
 * </blockquote>
 *
 * <p>在将对象写入流时，需要指定一个替代对象的序列化类应该实现这个特殊的方法，具体签名如下：
 * <pre>
 *     ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
 * </pre>
 * <p>如果writeReplace方法存在，则序列化将会调用该方法，并且可以从被序列化对象的类中定义
 * 的方法访问。因此，该方法可以具有 private， protected 和 package-private 访问。
 * 子类访问此方法遵循java可访问性规则。
 *
 * <p>当从流中读取的例时，需要指定替换的类应该使用确切的签名实现此特殊方法。(单例模式，
 * 对于一些singleton class，如果你让其implements Serializable，会导致该class不再是
 * singleton。使用ObjectInputStream.readObject()读取进来之后，如果是多次读取，
 * 就会创建多个object，解决的办法之一就是override一个 method，readResolve())
 * <pre>
 *     ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
 * </pre>
 *
 * <p>此readResolve方法遵循与writeReplace相同的调用规则和可访问性规则。
 *
 * <p>序列化运行时将每个序列化类与版本号相关联，成为serialVersionUID，在反序列化期间使用
 * 该版本号来验证序列化对象的发送方和接收方是否已加载与该序列化兼容的该对象的类。如果接收方具有
 * 不同于发送方类的 serialVersionUID 的对象加载了类，那么反序列化将导致{@link InvalidClassException}。
 * 一个可序列化的类通过声明名为<code>"serialVersionUID"</code>的字段显式声明他自己的
 * serialVersionUID，该字段必须是 static ， final的，类型必须为<code>long</code>：
 * <pre>
 *     ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;
 * </pre>
 *
 * <p>如果可序列化类没有显式声明serialVersionUID，则序列化运行期间将会基于类的各方面计算该类
 * 的默认serialVersionUID值，如Java(TM) 对象序列化规范中所描述。但是，<em>强烈建议</em>所有
 * 的可序列化类显式声明serialVersionUID值，因此默认的serialVersionUID计算对类的细节高度敏感，
 * 这些细节可能因编译器实现有差异，因此在反序列化期间可能会导致意外的<code>InvalidClassException</code>。
 * 因此，为了保证跨不同的java编译器实现一致的serialVersionUID值，可序列化必须显式声明serialVersionUID值。
 * 强烈建议显式serialVersionUID声明尽可能使用 <code>private</code> 修饰符，因为这样的声明
 * 只适用于当前声明的类——serialVersionUID 字段作为继承成员是没有用的。数组类不能显式声明
 * serialVersionUID，因此它们始终具有默认的计算值，但是对于数组类，不需要匹配serialVersionUID值。
 *
 * @author  unascribed
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see java.io.ObjectOutput
 * @see java.io.ObjectInput
 * @see java.io.Externalizable
 * @since   1.1
 */
public interface Serializable {
}
