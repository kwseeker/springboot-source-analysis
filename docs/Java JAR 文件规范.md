# Java Jar 文件规范

Official Doc: 

规范：[JAR File Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html)

使用：[Using JAR Files: The Basics](https://docs.oracle.com/javase/tutorial/deployment/jar/basicsindex.html)



## Jar文件组成

在许多情况下，JAR 文件不仅仅是 Java 类文件和/或资源的简单存档。 它们用作应用程序和扩展的构建块。 META-INF 目录（如果存在）用于存储包和扩展配置数据，包括安全性、版本控制、扩展和服务。

即可能包含

+ **类文件**

+ **资源文件**

+ **META-INF拓展文件**

  + **MANIFEST.MF**

    用于定义扩展以及和包相关数据。

    由一个主节和一个单独的JAR文件条目的节列表组成，每个节由一个换行符分隔。

    出现在主节中的属性被称为主属性，而可以出现在各个节中的属性被称为每个条目属性。某些属性可以同时出现在主节和单独的节中，在这种情况下，每个条目的属性值将覆盖指定条目的主属性值。

    **主属性**：

    + Manifest-Version

      定义清单文件版本。

    + Created-By

      指定此清单生成依赖的Java版本和JDK供应商。该属性由 jar 工具生成。

    + Signature-Version
  
    Jar文件的签名版本。
  
  + Class-Path
  
    指定此应用程序或扩展需要的扩展或库的相对url。url由一个或多个空格分隔。应用程序或扩展类装入器使用此属性的值来构造其内部搜索路径。有关详细信息，请参阅类路径属性（[Class-Path Attribute](https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html#classpath)）一节。
  
  **应用程序属性**：此属性由捆绑到可执行 jar 文件中的独立应用程序使用，这些文件可由 java 运行时通过运行“java -jar x.jar”直接调用。
  
  + Main-Class
  
    此属性的值是启动器将在启动时加载的主应用程序类的类名（即Java -jar xxx.jar时的入口）。该值不得将 .class 扩展名附加到类名。
  
  **Applets属性**：已废弃，定义applet所依赖的扩展的需求、版本和位置信息。
  
    + Extension-List
  
    + <extension>-Extension-Name
    + <extension>-Specification-Version
    + <extension>-Implementation-Version
    + <extension>-Implementation-Vendor-Id
    + <extension>-Implementation-URL

    **为扩展标识定义的属性**：扩展使用此属性来定义其唯一标识。

    + Extension-Name

      此属性指定 Jar 文件中包含的扩展名。

    **为扩展和包版本控制和密封信息定义的属性**：这些属性定义了 JAR 文件所属的扩展的特性。这些属性的值适用于 JAR 文件中的所有包，但可以被每个条目的属性覆盖。

    + Implementation-Title
  + Implementation-Version
    + Implementation-Vendor
  + Implementation-Vendor-Id
    + Implementation-URL
  + Specification-Title
    + Specification-Version
  + Specification-Vendor
    + Sealed
  
    **MANIFEST.MF案例**
  
    ```properties
    # spring-context-5.2.16.RELEASE.jar的MANIFEST.MF
    Manifest-Version: 1.0
    Implementation-Title: spring-context
    Automatic-Module-Name: spring.context
    Implementation-Version: 5.2.16.RELEASE
    Created-By: 1.8.0_282 (Oracle Corporation)
    ```
  
  + **INDEX.LIST**
  
    此文件由 jar 工具的新“-i”选项生成，其中包含应用程序或扩展中定义的包的位置信息。 它是 JarIndex 实现的一部分，被类加载器用来加速他们的类加载过程。
  
  + **x.SF**
  
    JAR 文件的签名文件。  “x”代表基本文件名。
  
  + **x.DSA**
  
    与具有相同基本文件名的签名文件关联的签名块文件。该文件存储相应签名文件的数字签名。
  
  + **services/**
  
    该目录存储所有服务提供者配置文件。
  
    没错就是SPI用的那个定义接口实现类文件的目录。

> 清单文件和签名文件由一些RFC822标准的“name: value”对组成，被称为头信息或属性。
>
> 名称-值对组称为“节”。节与其他节由空行分隔。

