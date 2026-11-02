# GaruTool

开发环境需要 Node.js 24、pnpm 11、JDK 21 或更高版本，以及 Android SDK 36。

安装依赖后启动页面预览：

```sh
pnpm install
pnpm dev
```

浏览器可以预览页面、主题和设置选项。悬浮窗权限及系统动作需要在 Android 设备上验证。

## Android 构建

在 `android/local.properties` 中设置本机 SDK 路径：

```properties
sdk.dir=/path/to/Android/sdk
```

在项目根目录执行：

```sh
pnpm android:build
```

该命令构建网页、同步 Capacitor 并生成调试 APK，产物位于 `dist/`。应用版本取自 `package.json` 的 `version` 和 `androidVersionCode`，升级版本时同步更新这两个值。

使用 Android Studio 调试时，先同步网页和插件，再打开工程：

```sh
pnpm android:sync
pnpm android:open
```

## 修改功能

页面和交互在 `src/` 中开发。调整按钮名称、图标、悬浮条尺寸或主题时，编辑 `src/config/tools.ts`；SVG 源文件放在 `assets/icons/`。Android 构建会自动生成对应资源。

新增系统动作时，在动作配置中分配稳定 ID，并在 Android 的 `ToolActions.kt` 中实现执行逻辑。修改原生接口时，同时更新 `src/tools/contracts.ts` 和 `ToolsPlugin.kt`。

## 检查与调试

```sh
pnpm check
pnpm test
pnpm format:check
pnpm android:lint
```

首次运行 Android 检查前执行一次 `pnpm android:sync`。提交前在设备上检查按钮点击、拖动、旋转，以及从通知关闭悬浮窗后的页面状态。

调试包的网页可以通过 Chrome 的 `chrome://inspect` 检查。原生异常使用以下命令查看：

```sh
adb logcat -s GaruTool AndroidRuntime
```
