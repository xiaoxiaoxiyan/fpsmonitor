#!/bin/bash
set -e
mkdir -p /tmp/lib /tmp/lib-classes /tmp/aar-extract /tmp/aar-res /tmp/build/classes /tmp/build/dex /tmp/build/res-compiled /tmp/build/res-linked /tmp/build/gen

MC="https://maven.aliyun.com/repository/central"
MC2="https://maven.aliyun.com/repository/google"

echo "=== Downloading Kotlin ==="
curl -sfL "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-compiler-embeddable/2.0.21/kotlin-compiler-embeddable-2.0.21.jar" -o /tmp/lib/kotlin-compiler.jar
curl -sfL "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.0.21/kotlin-stdlib-2.0.21.jar" -o /tmp/lib/kotlin-stdlib.jar
curl -sfL "https://repo1.maven.org/maven2/org/jetbrains/annotations/13.0/annotations-13.0.jar" -o /tmp/lib/annotations.jar
curl -sfL "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.8.1/kotlinx-coroutines-core-jvm-1.8.1.jar" -o /tmp/lib/coroutines.jar
curl -sfL "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-reflect/2.0.21/kotlin-reflect-2.0.21.jar" -o /tmp/lib/kotlin-reflect.jar
curl -sfL "https://repo1.maven.org/maven2/org/jetbrains/kotlin/compose-compiler-plugin-hosted/2.0.21/compose-compiler-plugin-hosted-2.0.21.jar" -o /tmp/lib/compose-compiler-plugin.jar || true
echo "Kotlin done"

echo "=== Downloading AndroidX Core ==="
curl -sfL "$MC/androidx/core/core-ktx/1.15.0/core-ktx-1.15.0.aar" -o /tmp/lib/core-ktx.aar || echo "SKIP core-ktx"
curl -sfL "$MC/androidx/core/core/1.15.0/core-1.15.0.aar" -o /tmp/lib/core.aar || echo "SKIP core"
curl -sfL "$MC/androidx/annotation/annotation/1.9.1/annotation-1.9.1.jar" -o /tmp/lib/annotation.jar || echo "SKIP annotation"
curl -sfL "$MC/androidx/annotation/annotation-jvm/1.9.1/annotation-jvm-1.9.1.jar" -o /tmp/lib/annotation-jvm.jar || echo "SKIP annotation-jvm"

echo "=== Downloading Lifecycle ==="
curl -sfL "$MC/androidx/lifecycle/lifecycle-runtime-ktx/2.8.7/lifecycle-runtime-ktx-2.8.7.aar" -o /tmp/lib/lifecycle-runtime-ktx.aar || echo "SKIP lifecycle-runtime-ktx"
curl -sfL "$MC/androidx/lifecycle/lifecycle-runtime/2.8.7/lifecycle-runtime-2.8.7.aar" -o /tmp/lib/lifecycle-runtime.aar || echo "SKIP lifecycle-runtime"
curl -sfL "$MC/androidx/lifecycle/lifecycle-viewmodel/2.8.7/lifecycle-viewmodel-2.8.7.aar" -o /tmp/lib/lifecycle-viewmodel.aar || echo "SKIP lifecycle-viewmodel"
curl -sfL "$MC/androidx/lifecycle/lifecycle-viewmodel-compose/2.8.7/lifecycle-viewmodel-compose-2.8.7.aar" -o /tmp/lib/lifecycle-viewmodel-compose.aar || echo "SKIP lifecycle-viewmodel-compose"
curl -sfL "$MC/androidx/lifecycle/lifecycle-livedata/2.8.7/lifecycle-livedata-2.8.7.aar" -o /tmp/lib/lifecycle-livedata.aar || echo "SKIP lifecycle-livedata"
curl -sfL "$MC/androidx/lifecycle/lifecycle-livedata-core/2.8.7/lifecycle-livedata-core-2.8.7.aar" -o /tmp/lib/lifecycle-livedata-core.aar || echo "SKIP lifecycle-livedata-core"
curl -sfL "$MC/androidx/lifecycle/lifecycle-viewmodel-savedstate/2.8.7/lifecycle-viewmodel-savedstate-2.8.7.aar" -o /tmp/lib/lifecycle-viewmodel-savedstate.aar || echo "SKIP lifecycle-viewmodel-savedstate"
curl -sfL "$MC/androidx/lifecycle/lifecycle-common/2.8.7/lifecycle-common-2.8.7.jar" -o /tmp/lib/lifecycle-common.jar || echo "SKIP lifecycle-common"

echo "=== Downloading Activity ==="
curl -sfL "$MC/androidx/activity/activity/1.9.3/activity-1.9.3.aar" -o /tmp/lib/activity.aar || echo "SKIP activity"
curl -sfL "$MC/androidx/activity/activity-compose/1.9.3/activity-compose-1.9.3.aar" -o /tmp/lib/activity-compose.aar || echo "SKIP activity-compose"
curl -sfL "$MC/androidx/activity/activity-ktx/1.9.3/activity-ktx-1.9.3.aar" -o /tmp/lib/activity-ktx.aar || echo "SKIP activity-ktx"

echo "=== Downloading Compose ==="
curl -sfL "$MC/androidx/compose/runtime/runtime/1.7.6/runtime-1.7.6.aar" -o /tmp/lib/compose-runtime.aar || echo "SKIP compose-runtime"
curl -sfL "$MC/androidx/compose/runtime/runtime-saveable/1.7.6/runtime-saveable-1.7.6.aar" -o /tmp/lib/compose-runtime-saveable.aar || echo "SKIP compose-runtime-saveable"
curl -sfL "$MC/androidx/compose/ui/ui/1.7.6/ui-1.7.6.aar" -o /tmp/lib/compose-ui.aar || echo "SKIP compose-ui"
curl -sfL "$MC/androidx/compose/ui/ui-geometry/1.7.6/ui-geometry-1.7.6.aar" -o /tmp/lib/compose-ui-geometry.aar || echo "SKIP compose-ui-geometry"
curl -sfL "$MC/androidx/compose/ui/ui-graphics/1.7.6/ui-graphics-1.7.6.aar" -o /tmp/lib/compose-ui-graphics.aar || echo "SKIP compose-ui-graphics"
curl -sfL "$MC/androidx/compose/ui/ui-text/1.7.6/ui-text-1.7.6.aar" -o /tmp/lib/compose-ui-text.aar || echo "SKIP compose-ui-text"
curl -sfL "$MC/androidx/compose/ui/ui-unit/1.7.6/ui-unit-1.7.6.aar" -o /tmp/lib/compose-ui-unit.aar || echo "SKIP compose-ui-unit"
curl -sfL "$MC/androidx/compose/ui/ui-util/1.7.6/ui-util-1.7.6.aar" -o /tmp/lib/compose-ui-util.aar || echo "SKIP compose-ui-util"
curl -sfL "$MC/androidx/compose/foundation/foundation/1.7.6/foundation-1.7.6.aar" -o /tmp/lib/compose-foundation.aar || echo "SKIP compose-foundation"
curl -sfL "$MC/androidx/compose/foundation/foundation-layout/1.7.6/foundation-layout-1.7.6.aar" -o /tmp/lib/compose-foundation-layout.aar || echo "SKIP compose-foundation-layout"
curl -sfL "$MC/androidx/compose/animation/animation/1.7.6/animation-1.7.6.aar" -o /tmp/lib/compose-animation.aar || echo "SKIP compose-animation"
curl -sfL "$MC/androidx/compose/animation/animation-core/1.7.6/animation-core-1.7.6.aar" -o /tmp/lib/compose-animation-core.aar || echo "SKIP compose-animation-core"
curl -sfL "$MC/androidx/compose/material3/material3/1.3.1/material3-1.3.1.aar" -o /tmp/lib/compose-material3.aar || echo "SKIP compose-material3"
curl -sfL "$MC/androidx/compose/material/material-icons-core/1.7.6/material-icons-core-1.7.6.aar" -o /tmp/lib/material-icons-core.aar || echo "SKIP material-icons-core"
curl -sfL "$MC/androidx/compose/material/material-icons-extended/1.7.6/material-icons-extended-1.7.6.aar" -o /tmp/lib/material-icons-extended.aar || echo "SKIP material-icons-extended"
curl -sfL "$MC/androidx/compose/material/material/1.7.6/material-1.7.6.aar" -o /tmp/lib/compose-material.aar || echo "SKIP compose-material"
curl -sfL "$MC/androidx/compose/material/material-ripple/1.7.6/material-ripple-1.7.6.aar" -o /tmp/lib/compose-material-ripple.aar || echo "SKIP compose-material-ripple"

echo "=== Downloading Navigation ==="
curl -sfL "$MC/androidx/navigation/navigation-compose/2.8.5/navigation-compose-2.8.5.aar" -o /tmp/lib/navigation-compose.aar || echo "SKIP navigation-compose"
curl -sfL "$MC/androidx/navigation/navigation-runtime/2.8.5/navigation-runtime-2.8.5.aar" -o /tmp/lib/navigation-runtime.aar || echo "SKIP navigation-runtime"
curl -sfL "$MC/androidx/navigation/navigation-runtime-ktx/2.8.5/navigation-runtime-ktx-2.8.5.aar" -o /tmp/lib/navigation-runtime-ktx.aar || echo "SKIP navigation-runtime-ktx"
curl -sfL "$MC/androidx/navigation/navigation-common/2.8.5/navigation-common-2.8.5.aar" -o /tmp/lib/navigation-common.aar || echo "SKIP navigation-common"
curl -sfL "$MC/androidx/navigation/navigation-common-ktx/2.8.5/navigation-common-ktx-2.8.5.aar" -o /tmp/lib/navigation-common-ktx.aar || echo "SKIP navigation-common-ktx"

echo "=== Downloading SavedState ==="
curl -sfL "$MC/androidx/savedstate/savedstate/1.2.1/savedstate-1.2.1.aar" -o /tmp/lib/savedstate.aar || echo "SKIP savedstate"
curl -sfL "$MC/androidx/savedstate/savedstate-ktx/1.2.1/savedstate-ktx-1.2.1.aar" -o /tmp/lib/savedstate-ktx.aar || echo "SKIP savedstate-ktx"

echo "=== Downloading Other AndroidX ==="
curl -sfL "$MC/androidx/concurrent/concurrent-futures/1.2.0/concurrent-futures-1.2.0.jar" -o /tmp/lib/concurrent-futures.jar || echo "SKIP concurrent-futures"
curl -sfL "$MC/androidx/interpolator/interpolator/1.0.0/interpolator-1.0.0.aar" -o /tmp/lib/interpolator.aar || echo "SKIP interpolator"
curl -sfL "$MC/androidx/loader/loader/1.0.0/loader-1.0.0.aar" -o /tmp/lib/loader.aar || echo "SKIP loader"
curl -sfL "$MC/androidx/profileinstaller/profileinstaller/1.4.1/profileinstaller-1.4.1.aar" -o /tmp/lib/profileinstaller.aar || echo "SKIP profileinstaller"
curl -sfL "$MC/androidx/startup/startup-runtime/1.1.1/startup-runtime-1.1.1.aar" -o /tmp/lib/startup-runtime.aar || echo "SKIP startup-runtime"
curl -sfL "$MC/androidx/versionedparcelable/versionedparcelable/1.1.1/versionedparcelable-1.1.1.aar" -o /tmp/lib/versionedparcelable.aar || echo "SKIP versionedparcelable"
curl -sfL "$MC/androidx/collection/collection/1.4.5/collection-1.4.5.jar" -o /tmp/lib/collection.jar || echo "SKIP collection"
curl -sfL "$MC/androidx/collection/collection-ktx/1.4.5/collection-ktx-1.4.5.jar" -o /tmp/lib/collection-ktx.jar || echo "SKIP collection-ktx"
curl -sfL "$MC/androidx/arch/core/core-runtime/2.2.0/core-runtime-2.2.0.aar" -o /tmp/lib/core-runtime.aar || echo "SKIP core-runtime"
curl -sfL "$MC/androidx/arch/core/core-common/2.2.0/core-common-2.2.0.jar" -o /tmp/lib/core-common.jar || echo "SKIP core-common"
curl -sfL "$MC/androidx/tracing/tracing/1.2.0/tracing-1.2.0.aar" -o /tmp/lib/tracing.aar || echo "SKIP tracing"
curl -sfL "$MC/androidx/emoji2/emoji2/1.3.0/emoji2-1.3.0.aar" -o /tmp/lib/emoji2.aar || echo "SKIP emoji2"
curl -sfL "$MC/androidx/customview/customview/1.0.0/customview-1.0.0.aar" -o /tmp/lib/customview.aar || echo "SKIP customview"
curl -sfL "$MC/androidx/viewpager/viewpager/1.0.0/viewpager-1.0.0.aar" -o /tmp/lib/viewpager.aar || echo "SKIP viewpager"
curl -sfL "$MC/androidx/slidingpanelayout/slidingpanelayout/1.2.0/slidingpanelayout-1.2.0.aar" -o /tmp/lib/slidingpanelayout.aar || echo "SKIP slidingpanelayout"
curl -sfL "$MC/androidx/window/window/1.3.0/window-1.3.0.aar" -o /tmp/lib/window.aar || echo "SKIP window"
curl -sfL "$MC/androidx/window/extensions/window-extensions/1.0.0/window-extensions-1.0.0.aar" -o /tmp/lib/window-extensions.aar || echo "SKIP window-extensions"
curl -sfL "$MC/androidx/autofill/autofill/1.1.0/autofill-1.1.0.aar" -o /tmp/lib/autofill.aar || echo "SKIP autofill"
curl -sfL "$MC/androidx/compose/ui/ui-tooling-preview/1.7.6/ui-tooling-preview-1.7.6.aar" -o /tmp/lib/compose-ui-tooling-preview.aar || echo "SKIP compose-ui-tooling-preview"
curl -sfL "$MC/androidx/compose/ui/ui-viewbinding/1.7.6/ui-viewbinding-1.7.6.aar" -o /tmp/lib/compose-ui-viewbinding.aar || echo "SKIP compose-ui-viewbinding"
curl -sfL "$MC/androidx/compose/runtime/runtime-livedata/1.7.6/runtime-livedata-1.7.6.aar" -o /tmp/lib/compose-runtime-livedata.aar || echo "SKIP compose-runtime-livedata"

echo "=== Downloading Material & AppCompat ==="
curl -sfL "$MC/com/google/android/material/material/1.12.0/material-1.12.0.aar" -o /tmp/lib/material.aar || echo "SKIP material"
curl -sfL "$MC/androidx/appcompat/appcompat/1.7.0/appcompat-1.7.0.aar" -o /tmp/lib/appcompat.aar || echo "SKIP appcompat"
curl -sfL "$MC/androidx/appcompat/appcompat-resources/1.7.0/appcompat-resources-1.7.0.aar" -o /tmp/lib/appcompat-resources.aar || echo "SKIP appcompat-resources"
curl -sfL "$MC/androidx/vectordrawable/vectordrawable/1.1.0/vectordrawable-1.1.0.aar" -o /tmp/lib/vectordrawable.aar || echo "SKIP vectordrawable"
curl -sfL "$MC/androidx/vectordrawable/vectordrawable-animated/1.1.0/vectordrawable-animated-1.1.0.aar" -o /tmp/lib/vectordrawable-animated.aar || echo "SKIP vectordrawable-animated"
curl -sfL "$MC/androidx/fragment/fragment/1.5.4/fragment-1.5.4.aar" -o /tmp/lib/fragment.aar || echo "SKIP fragment"
curl -sfL "$MC/androidx/drawerlayout/drawerlayout/1.1.1/drawerlayout-1.1.1.aar" -o /tmp/lib/drawerlayout.aar || echo "SKIP drawerlayout"
curl -sfL "$MC/androidx/coordinatorlayout/coordinatorlayout/1.1.0/coordinatorlayout-1.1.0.aar" -o /tmp/lib/coordinatorlayout.aar || echo "SKIP coordinatorlayout"
curl -sfL "$MC/androidx/swiperefreshlayout/swiperefreshlayout/1.1.0/swiperefreshlayout-1.1.0.aar" -o /tmp/lib/swiperefreshlayout.aar || echo "SKIP swiperefreshlayout"
curl -sfL "$MC/androidx/recyclerview/recyclerview/1.1.0/recyclerview-1.1.0.aar" -o /tmp/lib/recyclerview.aar || echo "SKIP recyclerview"
curl -sfL "$MC/androidx/transition/transition/1.5.0/transition-1.5.0.aar" -o /tmp/lib/transition.aar || echo "SKIP transition"
curl -sfL "$MC/androidx/viewpager2/viewpager2/1.1.0/viewpager2-1.1.0.aar" -o /tmp/lib/viewpager2.aar || echo "SKIP viewpager2"
curl -sfL "$MC/androidx/cardview/cardview/1.0.0/cardview-1.0.0.aar" -o /tmp/lib/cardview.aar || echo "SKIP cardview"
curl -sfL "$MC/androidx/constraintlayout/constraintlayout/2.2.0/constraintlayout-2.2.0.aar" -o /tmp/lib/constraintlayout.aar || echo "SKIP constraintlayout"
curl -sfL "$MC/androidx/constraintlayout/constraintlayout-core/1.1.0/constraintlayout-core-1.1.0.jar" -o /tmp/lib/constraintlayout-core.jar || echo "SKIP constraintlayout-core"

echo "=== Downloading OkHttp ==="
curl -sfL "$MC/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar" -o /tmp/lib/okhttp.jar || echo "SKIP okhttp"
curl -sfL "$MC/com/squareup/okio/okio-jvm/3.6.0/okio-jvm-3.6.0.jar" -o /tmp/lib/okio.jar || echo "SKIP okio"

echo "=== Downloading libsu ==="
curl -sfL "$MC/com/github/topjohnwu/libsu/core/5.2.2/core-5.2.2.aar" -o /tmp/lib/libsu-core.aar || echo "SKIP libsu-core"
curl -sfL "$MC/com/github/topjohnwu/libsu/service/5.2.2/service-5.2.2.aar" -o /tmp/lib/libsu-service.aar || echo "SKIP libsu-service"
curl -sfL "$MC/com/github/topjohnwu/libsu/nio/5.2.2/nio-5.2.2.aar" -o /tmp/lib/libsu-nio.aar || echo "SKIP libsu-nio"
curl -sfL "$MC/com/github/topjohnwu/libsu/io/5.2.2/io-5.2.2.aar" -o /tmp/lib/libsu-io.aar || echo "SKIP libsu-io"

echo "=== Downloading zip4j & commons ==="
curl -sfL "$MC/net/lingala/zip4j/zip4j/2.11.5/zip4j-2.11.5.jar" -o /tmp/lib/zip4j.jar || echo "SKIP zip4j"
curl -sfL "$MC/org/apache/commons/commons-compress/1.26.0/commons-compress-1.26.0.jar" -o /tmp/lib/commons-compress.jar || echo "SKIP commons-compress"
curl -sfL "$MC/commons-codec/commons-codec/1.16.0/commons-codec-1.16.0.jar" -o /tmp/lib/commons-codec.jar || echo "SKIP commons-codec"
curl -sfL "$MC/org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar" -o /tmp/lib/commons-lang3.jar || echo "SKIP commons-lang3"

echo "=== Downloading Guava ==="
curl -sfL "$MC/com/google/guava/guava/33.0.0-android/guava-33.0.0-android.jar" -o /tmp/lib/guava.jar || echo "SKIP guava"
curl -sfL "$MC/com/google/guava/failureaccess/1.0.2/failureaccess-1.0.2.jar" -o /tmp/lib/failureaccess.jar || echo "SKIP failureaccess"

echo "=== All downloads complete ==="
ls -la /tmp/lib/
