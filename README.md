# HybriDroid [![Build Status](https://travis-ci.org/SunghoLee/HybriDroid.svg?branch=master)](https://travis-ci.org/SunghoLee/HybriDroid)

A static analysis framework to analyze Android hybrid applications.

## Getting Started

### Prerequisites

To build and execute HybriDroid, Gradle and Java are required.

HybriDroid is tested under

* JDK 1.8.0
* Gradle 4.3.1

### Building HybriDroid

To build HybriDroid, execute the build.sh script.

```
bash-3.2$ ./build.sh
```

It tries to build HybriDroid using Gradle, and below message would be shown if the build is successful.

```
BUILD SUCCESSFUL in [xx]s
```

The build script creates hybridroid.jar file in the directory in which the script is.

## Running HybriDroid

Java Runtime directory and Android rt file must be set in wala.properties file like the below example. Android rt file is included in HybriDroid project.

```
// Java runtime directory
java_runtime_dir = /Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/jre/lib
// Android rt directory
android_rt_jar = /Users/leesh/Documents/repo/HybriDroid/data/android.jar
```

Then, HybriDroid runs using below command.

```
java -jar hybridroid.jar -cfg -t ../HelloHybrid.apk -p wala.properties
```

## WALA

HybriDroid is implemented and integrated in WALA. For more details on WALA, see <a
href="http://wala.sourceforge.net">the WALA home page</a>.

## License

This project is licensed under the Eclipse Public License v1.0
