#android studio 中，APP_ABI会被忽略，不起作用 build.gradle  在 android { defualtconfig 中添加 ndk {abiFilters  "arm64-v8a"
APP_ABI := arm64-v8a