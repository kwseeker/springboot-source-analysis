# Spring Boot 启动流程&退出流程

## Spring Boot Jar包

一个可以直接通过java -jar启动的Spring Boot JAR包的结构：

```yaml
# 仅仅只有一个Controller的spring-boot-web应用程序

spring_initializer-0.0.1-SNAPSHOT
├── BOOT-INF
│   ├── classes						# 应用程序类
│   │   ├── application.properties
│   │   ├── banner.jpg
│   │   └── top
│   │       └── kwseeker
│   │           ├── controllers
│   │           │   └── HelloController.class
│   │           └── SpringInitializerApplication.class
│   ├── classpath.idx
│   └── lib							# Spring Boot 第三方依赖
│       ├── jackson-annotations-2.11.3.jar
│       ├── ...
│       ├── snakeyaml-1.26.jar
│       ├── spring-aop-5.2.11.RELEASE.jar
│       ├── spring-beans-5.2.11.RELEASE.jar
│       ├── spring-boot-2.3.6.RELEASE.jar
│       ├── spring-boot-autoconfigure-2.3.6.RELEASE.jar
│       ├── spring-boot-starter-2.3.6.RELEASE.jar
│       ├── spring-boot-starter-json-2.3.6.RELEASE.jar
│       ├── ...
│       ├── tomcat-embed-core-9.0.39.jar
│       └── tomcat-embed-websocket-9.0.39.jar
├── META-INF
│   ├── MANIFEST.MF					# Spring Boot 应用清单文件
│   └── maven
│       └── top.kwseeker.springboot
│           └── spring_initializer
│               ├── pom.properties
│               └── pom.xml
└── org	
    └── springframework
        └── boot
            └── loader				# Spring Boot 类加载器
                ├── archive
                │   ├── Archive.class
                │   ├── ...
                │   ├── ExplodedArchive$SimpleJarFileArchive.class
                │   ├── JarFileArchive$AbstractIterator.class
                │   ├── JarFileArchive.class
                │   ├── JarFileArchive$EntryIterator.class
                │   ├── JarFileArchive$JarFileEntry.class
                │   └── JarFileArchive$NestedArchiveIterator.class
                ├── ClassPathIndexFile.class
                ├── data
                │   ├── RandomAccessData.class
                │   ├── RandomAccessDataFile$1.class
                │   ├── RandomAccessDataFile.class
                │   ├── RandomAccessDataFile$DataInputStream.class
                │   └── RandomAccessDataFile$FileAccess.class
                ├── ExecutableArchiveLauncher.class
                ├── jar
                │   ├── AbstractJarFile.class
                │   ├── ...
                │   ├── JarFile.class
                │   ├── JarFileEntries$1.class
                │   ├── JarFileEntries.class
                │   ├── JarFileEntries$EntryIterator.class
                │   ├── JarFile$JarEntryEnumeration.class
                │   ├── JarFileWrapper.class
                │   ├── JarURLConnection$1.class
                │   ├── JarURLConnection.class
                │   ├── JarURLConnection$JarEntryName.class
                │   ├── StringSequence.class
                │   └── ZipInflaterInputStream.class
                ├── JarLauncher.class
                ├── jarmode
                │   ├── JarMode.class
                │   ├── JarModeLauncher.class
                │   └── TestJarMode.class
                ├── LaunchedURLClassLoader.class
                ├── LaunchedURLClassLoader$DefinePackageCallType.class
                ├── LaunchedURLClassLoader$UseFastConnectionExceptionsEnumeration.class
                ├── Launcher.class
                ├── MainMethodRunner.class
                ├── PropertiesLauncher$1.class
                ├── PropertiesLauncher$ArchiveEntryFilter.class
                ├── PropertiesLauncher.class
                ├── PropertiesLauncher$ClassPathArchives.class
                ├── PropertiesLauncher$PrefixMatchingArchiveFilter.class
                ├── util
                │   └── SystemPropertyUtils.class
                └── WarLauncher.class
```

案例Jar的MANIFEST.MF

```yml
Manifest-Version: 1.0
Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
Implementation-Title: spring_initializr
Implementation-Version: 0.0.1-SNAPSHOT
# Spring Boot 应用启动类
Start-Class: top.kwseeker.SpringInitializerApplication
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Build-Jdk-Spec: 1.8
Spring-Boot-Version: 2.3.6.RELEASE
Created-By: Maven Jar Plugin 3.2.0
# spring boot 打包时会自动创建一个类加载器JarLauncher, java -jar 执行时会执行JarLauncher的main方法,加载Spring Boot主类并嵌套执行
Main-Class: org.springframework.boot.loader.JarLauncher
```

spring-boot-maven-plugin打包原理

spring-boot-maven-plugin默认有5个goals：repackage、run、start、stop、build-info。在打包的时候默认使用的repackage。spring-boot-maven-plugin的repackage能够将mvn package生成的软件包，再次打包为可执行的软件包（称为Fat Jar），并将mvn package生成的软件包重命名为*.original。

大概可以理解为Spring Boot 重新封装了Jar包，因为某些定义不是Java标准定义，所以需要自行解析处理，JarLauncher就是自行解析的入口。

大概流程（后面有时间再详细看这部分）：

1）FatJar的启动Main函数是JarLauncher，以FatJar(如上面的spring_initializer-0.0.1-SNAPSHOT.jar)为file作为入参，构造JarFileArchive对象。JarFileArchive对象获取FatJar中的资源文件，获取其URL，构建URLClassLoader;

2）使用URLClassLoader加载lib下的jar, 读取MANIFEST.MF文件中Start-Class指向的业务类，以一个新线程执行静态方法main，启动程序。

