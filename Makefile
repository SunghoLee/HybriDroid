BASH := bash
GRD := gradle
GRDW := gradlew
MVN := mvn 
GIT := git

WALA_DIR := WALA
WALA_CORE := $(WALA_DIR)/com.ibm.wala.core
WALA_RHINO :=$(WALA_DIR)/com.ibm.wala.cast.js.rhino
WALA_UTIL := $(WALA_DIR)/com.ibm.wala.util
WALA_NU := $(WALA_DIR)/com.ibm.wala.cast.js.html.nu_validator
WALA_CAST := $(WALA_DIR)/com.ibm.wala.cast
WALA_DALVIK := $(WALA_DIR)/com.ibm.wala.dalvik
WALA_RHINO_TEST := $(WALA_DIR)/com.ibm.wala.cast.js.rhino.test
WALA_SHRIKE := $(WALA_DIR)/com.ibm.wala.shrike
WALA_JS := $(WALA_DIR)/com.ibm.wala.cast.js
WALA_JS_TEST := $(WALA_DIR)/com.ibm.wala.cast.js.test
WALA_JAVA := $(WALA_DIR)/com.ibm.wala.cast.java
WALA_CORE_TEST := $(WALA_DIR)/com.ibm.wala.core.tests

WALA_TARGETS := $(WALA_CORE)/target $(WALA_RHINO)/target $(WALA_UTIL)/target $(WALA_NU)/target $(WALA_CAST)/target $(WALA_DALVIK)/target $(WALA_RHINO_TEST)/target $(WALA_SHRIKE)/target $(WALA_JS)/target $(WALA_JS_TEST)/target $(WALA_JAVA)/target $(WALA_CORE_TEST)/target

HYBRIDROID := kr.ac.kaist.wala.hybridroid/build/libs/kr.ac.kaist.wala.hybridroid.jar
HYBRIDROID_TARGET := hybridroid.jar 
HYBRIDROID_TEST := kr.ac.kaist.wala.hybridroid.test

$(HYBRIDROID_TARGET): $(HYBRIDROID)
	cp $(HYBRIDROID) $(HYBRIDROID_TARGET) 

$(HYBRIDROID): $(WALA_TARGETS)
	$(BASH) $(GRDW) build -x test

$(WALA_TARGETS): $(WALA_DIR)
	$(MVN) clean install -DskipTests -B -q -f $(WALA_DIR)/pom.xml

$(WALA_DIR):
	$(GIT) clone --depth=50 https://github.com/wala/WALA $(WALA_DIR)

clean:
	$(BASH) $(GRDW) clean -PwalaDir=$(WALA_DIR)
	rm $(HYBRIDROID_TARGET)

test: 
	$(BASH) $(GRDW) cleanTest test -b $(HYBRIDROID_TEST)/build.gradle

.PHONY: compile clean test
.SILENT: compile $(WALA_TARGETS) $(WALA_DIR) clean $(HYBRIDROID_TARGET) $(HYBRIDROID) test
