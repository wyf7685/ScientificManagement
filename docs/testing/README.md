# ScienceManager 测试执行包

本目录用于存放 SIT 联调阶段的核心功能测试资产，供测试经理、测试工程师和研发联调时直接使用。

## 文件说明

- `SIT核心功能测试执行清单.md`
  - 按执行顺序组织的测试步骤，适合作为每日测试 runbook。
- `SIT核心功能测试用例表.csv`
  - 可直接导入表格工具维护执行结果、责任人和证据链接。
- `缺陷记录模板.md`
  - 标准缺陷单模板，包含字段说明、填写规范和示例。
- `缺陷记录模板.csv`
  - 缺陷台账空模板，可直接导入 Excel、Numbers 或飞书表格。

## 使用建议

1. 每次进入 SIT 前先复制一份 `SIT核心功能测试用例表.csv` 作为当轮执行台账。
2. 执行过程中按用例 ID 回填实际结果、执行人、执行日期、证据链接和缺陷 ID。
3. 发现问题后，按 `缺陷记录模板.md` 的格式提交缺陷，并同步写入 `缺陷记录模板.csv`。
4. 过程系统联调用例需同时参考 [achmanager-backend/docs/过程系统API测试文档.md](/Users/junhaowu/Desktop/tongxiang_project/ScienceManager/achmanager-backend/docs/过程系统API测试文档.md)。

## 适用范围

- 前端：`research-management-system`
- 后端：`achmanager-backend`
- CMS：`my-strapi-project`
- 外部集成：Keycloak、MySQL、Redis、过程系统 API

## 本包默认假设

- 默认执行环境为 SIT。
- 默认仅覆盖核心功能和非侵入式安全验证。
- 默认不执行破坏性压测和高风险渗透测试。
