ANDROID := $(ANDROID_HOME)/tools/android
ADB := $(ANDROID_HOME)/platform-tools/adb

API := android-19

ANDROIDAPIS := $(API) # Add others as required.

ANDROIDSOURCES := $(addprefix $(ANDROID_HOME)/sources/,$(ANDROIDAPIS))

PACKAGE := com.qrclab.ncouragr

MAYBE_RESET_USB := test "$(ANDROID_USB)" && $(ADB) usb && sleep 1 || true

OSASCRIPT := /usr/bin/osascript -e

# Task names extracted from output of './gradlew tasks' and re-ordered
# slightly.
#
GRADLETASKS :=\
	help\
	tasks\
	androidDependencies\
	signingReport\
	assemble\
	assembleDebug\
	assembleDebugTest\
	assembleRelease\
	build\
	buildDependents\
	buildNeeded\
	clean\
	init\
	wrapper\
	dependencies\
	dependencyInsight\
	projects\
	properties\
	installDebug\
	installDebugTest\
	uninstallAll\
	uninstallDebug\
	uninstallDebugTest\
	uninstallRelease\
	check\
	connectedAndroidTest\
	connectedCheck\
	deviceCheck\
	#


$(GRADLETASKS):
	./gradlew $@
.PHONY: $(GRADLETASKS)

define HEY
$(OSASCRIPT) 'display notification"'"$(1)"'"with title "''Hey Human!''"'
endef

debug:
	./gradlew app:installDebug
	$(MAYBE_RESET_USB)
	$(ADB) logcat -c
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	$(call HEY, Android is done.)
	$(ADB) logcat
	# $(ADB) shell pm dump $(PACKAGE)
.PHONY: debug

mobile:
	./gradlew mobile:installDebug
	$(MAYBE_RESET_USB)
	$(ADB) logcat -c
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	$(call HEY, Android $@ is ready.)
	$(ADB) logcat
	# $(ADB) shell pm dump $(PACKAGE)
.PHONY: mobile

wearable:
	./gradlew wearable:installDebug
	$(MAYBE_RESET_USB)
	$(ADB) logcat -c
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	$(call HEY, Android $@ is ready.)
	$(ADB) logcat
	# $(ADB) shell pm dump $(PACKAGE)
.PHONY: wearable

bluetooth:			# Debug wearable over bluetooth.
	./gradlew wearable:installDebug
	$(MAYBE_RESET_USB)
	$(ADB) forward tcp:4444 localabstract:/adb-hub
	$(ADB) connect localhost:4444
	$(ADB) -s localhost:4444 logcat -c
	$(ADB) -s localhost:4444 shell pm path $(PACKAGE)
	$(ADB) -s localhost:4444 shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	$(call HEY, Android $@ is ready.)
	$(ADB) -s localhost:4444 logcat
	# $(ADB) -s localhost:4444 shell pm dump $(PACKAGE)
.PHONY: bluetooth


INSTRUMENT := $(PACKAGE).test/android.test.InstrumentationTestRunner
DEBUG_TEST_APK := app/build/outputs/apk/app-debug-test-unaligned.apk

test:
	./gradlew installDebugTest installDebug
	$(MAYBE_RESET_USB)
	@echo
	@echo Look for: Test results for InstrumentationTestRunner=.....
	@echo Look for: 'OK (5 tests)' ... for some value of 5
	@echo
	$(ADB) logcat -c
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell pm path $(PACKAGE).test
	$(ADB) shell pm list instrumentation
	$(ADB) shell pm list instrumentation -f
	$(ADB) shell am instrument -w -e target $(PACKAGE) $(INSTRUMENT)
	$(ADB) shell pm uninstall $(PACKAGE).test
	$(ADB) shell pm uninstall $(PACKAGE)
	$(ADB) logcat
.PHONY: test


dump: installDebug
	$(MAYBE_RESET_USB)
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell pm dump $(PACKAGE)
.PHONY: dump


monitor:
	$(ANDROID_HOME)/tools/monitor
.PHONY: monitor


bugreport:
	$(MAYBE_RESET_USB)
	$(ADB) bugreport > bugreport.txt
	gzip bugreport.txt
.PHONY: bugreport


lint lintDebug lintRelease:
	./gradlew $@
	open app/build/outputs/lint-results.html
.PHONY: lint lintDebug lintRelease


define TAGJAVA
find $(1) -type f -name '*.java' -print |\
xargs etags --append --output=$(2) &&
endef
TAGS tags: . $(LIBRARIES) $(ANDROIDSOURCES)
	rm -f TAGS.tmp
	$(foreach d,$^,$(call TAGJAVA,$(d),TAGS.tmp)) true
	cmp -s ./TAGS TAGS.tmp || rm -f ./TAGS && mv TAGS.tmp ./TAGS
.PHONY: TAGS tags


distclean: clean
	rm -f TAGS
.PHONY: distclean

# adb forward tcp:4444 localabstract:/adb-hub
# adb connect localhost:4444
