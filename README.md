# HybriDroid [![Build Status](https://travis-ci.org/SunghoLee/HybriDroid.svg?branch=master)](https://travis-ci.org/SunghoLee/HybriDroid)

A static analysis framework to analyze Android hybrid applications.

## Getting Started

### Prerequisites

To build and execute HybriDroid, Gradle and Java are required.

HybriDroid is tested under:

* JDK 1.8.0
* Gradle 4.3.1

HybriDroid is built on top of WALA requiring Maven and wget. WALA is automatically downloaded in the HybriDroid compilation process but Maven and wget are needed before the compilation.

### Building HybriDroid

HybriDroid is compiled as follow:

```
bash-3.2$ make
```

The ``Makefile`` clones WALA, compiles it, and compiles HybriDroid. When the compilation is done, ``hybridroid.jar`` is created in the root directory of HybriDroid project.

## Running HybriDroid

Java Runtime directory and Android rt file must be set in ``wala.properties`` as follow before running HybriDroid. Android rt file is included in HybriDroid project.

```
// Java runtime directory
java_runtime_dir = /Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/jre/lib
// Android rt directory
android_rt_jar = /Users/leesh/Documents/repo/HybriDroid/data/android.jar
```

Then, HybriDroid is executed as follow:

```
java -jar hybridroid.jar -cfg -t HelloHybrid.apk -p wala.properties
```

## WALA

HybriDroid is implemented and integrated in WALA. For more details on WALA, see <a
href="http://wala.sourceforge.net">the WALA home page</a>.

The current version of WALA requires build-tools-26.0.2 of Android sdk. If an error occurs in the WALA compilation, download Android sdk and build-tools-26.0.2 as well as add ``android-sdk/tools/bin`` to your PATH.

## License

This project is licensed under the Eclipse Public License v1.0
