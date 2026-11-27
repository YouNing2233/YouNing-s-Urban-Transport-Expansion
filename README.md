# USE —— YouNing's Urban Transport Expansion

基于 [Architectury](https://docs.architectury.dev/) 的 Minecraft 交通扩展模组，与天津地铁 MTR3 适配版（TJM）采用同一套多版本构建架构。

## 支持矩阵

| Minecraft | Fabric | Forge |
|---|---|---|
| 1.18.2 | ✅ | ✅ |
| 1.19.2 | ✅ | ✅ |
| 1.19.4 | ✅ | ✅ |
| 1.20.1 | ✅ | ✅ |

依赖：MTR（1.18.2–1.19.4 为 3.2.2-hotfix-1，1.20.1 为 3.6.2.2）、Architectury API、Cloth Config。

## 目录结构

```
common/             1.20.1 共享代码
common-1.18.2/      1.18.2 共享代码
common-1.19.2/      1.19.2 共享代码
common-1.19.4/      1.19.4 共享代码
fabric*/            Fabric 平台层（入口、资源、打包）
forge*/             Forge 平台层（入口、资源、打包）
```

## 构建

```bash
# 单个目标
./gradlew :fabric-1.18.2:build
./gradlew :forge:build

# MTR jar 不在默认位置时
./gradlew :fabric:build -PmtrJarDir=/path/to/mtr-jars
```

产物在 `build/`（根目录汇总）或各模块 `build/libs/`。

## 许可

待定（LICENSE 稍后补充）。
