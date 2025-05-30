---
layout: page
title: build
parent: 编辑器
nav_order: 2
---

# 编辑器 cfgeditor
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}
---

1. 可视化浏览表结构和记录
2. 编辑记录

## Prerequisites

1. nodejs, pnpm
2. `pnpm config set --global registry https://registry.npmmirror.com`
3. `pnpm install`


## 准备工作：启动对象数据服务器

```bash
java -jar ../cfggen.jar -datadir ../example/config  -gen server
```

## build

### 开发期，启动调试

```bash
pnpm run dev
```

使用浏览器查看 http://localhost:5173/


### 发布
```bash
pnpm run build
```

生成的页面和脚本在`dist`目录，可以运行
```bash
cd dist
jwebserver
```
来启动服务器进行测试，然后使用浏览器查看 http://localhost:8000/

实际部署请选择更成熟的web服务器。

## build exe

### Prerequisites
1. rust

### 生成cfgeditor.exe

```bash
pnpm tauri build
```

生成的cfgeditor.exe在 `src-tauri\target\release\`目录下