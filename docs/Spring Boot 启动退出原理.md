# Spring Boot 启动流程&退出流程

## Spring Boot Jar包

一个可以直接通过java -jar启动的Spring Boot JAR包的结构：

```yaml
# 仅仅只有一个Controller的spring-boot-web应用程序

spring_initializer-0.0.1-SNAPSHOT
├── BOOT-INF
│   ├── classes
│   │   ├── application.properties
│   │   ├── banner.jpg
│   │   └── top
│   │       └── kwseeker
│   │           ├── controllers
│   │           │   └── HelloController.class
│   │           └── SpringInitializerApplication.class
│   ├── classpath.idx
│   └── lib							# Spring Boot 依赖包
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

