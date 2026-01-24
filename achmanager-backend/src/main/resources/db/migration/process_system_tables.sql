-- 过程系统集成数据库表结构
-- 创建时间: 2026-01-23

-- 1. 过程系统项目提交物表
CREATE TABLE IF NOT EXISTS process_submissions (
    submission_id BIGINT PRIMARY KEY COMMENT '提交物唯一标识',
    application_id BIGINT NOT NULL COMMENT '外键 - 关联申报记录',
    submission_type VARCHAR(30) NOT NULL COMMENT '提交物类型 - proposal/application_attachment',
    submission_stage VARCHAR(30) NOT NULL COMMENT '提交阶段 - application',
    submission_round INT NOT NULL DEFAULT 1 COMMENT '提交轮次',
    submission_version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    
    -- 项目基本信息
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    project_field VARCHAR(100) COMMENT '项目所属领域',
    category_level VARCHAR(20) NOT NULL COMMENT '类别级别 - 重点/一般',
    category_specific VARCHAR(100) NOT NULL COMMENT '具体分类',
    research_period INT COMMENT '研究周期(月)',
    project_keywords VARCHAR(500) COMMENT '项目关键词',
    
    -- 项目内容信息
    project_description TEXT NOT NULL COMMENT '项目描述',
    expected_results TEXT COMMENT '预期成果',
    willing_adjust CHAR(1) COMMENT '是否愿意调整为一般项目',
    
    -- 申报人信息
    applicant_name VARCHAR(100) NOT NULL COMMENT '负责人姓名',
    id_card VARCHAR(30) COMMENT '证件号码',
    education_degree VARCHAR(50) COMMENT '学历学位',
    technical_title VARCHAR(50) COMMENT '技术职称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    work_unit VARCHAR(200) COMMENT '工作单位',
    unit_address VARCHAR(300) COMMENT '单位地址',
    representative_achievements TEXT COMMENT '申报人代表成果',
    
    -- 文件信息
    proposal_file_id VARCHAR(200) NOT NULL COMMENT '申报书文件ID',
    proposal_file_name VARCHAR(200) NOT NULL COMMENT '申报书文件名称',
    proposal_file_size BIGINT COMMENT '申报书文件大小(字节)',
    proposal_file_type VARCHAR(50) COMMENT '文件类型',
    proposal_file_url VARCHAR(500) COMMENT '申报书访问路径',
    other_attachments_json TEXT COMMENT '其他附件列表(JSON格式)',
    
    -- 上传信息
    uploader_id VARCHAR(64) NOT NULL COMMENT '上传者用户ID',
    uploader_name VARCHAR(100) NOT NULL COMMENT '上传者名称',
    upload_time DATETIME NOT NULL COMMENT '上传时间',
    submission_description TEXT COMMENT '提交物备注说明',
    
    -- 系统字段
    create_by VARCHAR(64) COMMENT '创建者ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    remark VARCHAR(500) COMMENT '备注',
    
    -- 同步字段
    sync_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同步时间',
    
    KEY idx_application_id (application_id),
    KEY idx_project_name (project_name),
    KEY idx_applicant_name (applicant_name),
    KEY idx_upload_time (upload_time),
    KEY idx_submission_type (submission_type),
    KEY idx_category_level (category_level),
    KEY idx_sync_time (sync_time)
) COMMENT='过程系统项目提交物表';

-- 2. 过程系统提交物文件表
CREATE TABLE IF NOT EXISTS process_submission_files (
    file_id VARCHAR(200) PRIMARY KEY COMMENT '文件唯一标识',
    submission_id BIGINT NOT NULL COMMENT '关联的提交物ID',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名称',
    original_name VARCHAR(200) COMMENT '文件原始名称',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_type VARCHAR(50) COMMENT '文件类型/扩展名',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_url VARCHAR(500) COMMENT '文件访问URL',
    file_category VARCHAR(30) COMMENT '文件分类 - proposal/attachment',
    file_description TEXT COMMENT '文件描述',
    file_md5 VARCHAR(32) COMMENT '文件MD5校验值',
    storage_status VARCHAR(20) DEFAULT 'uploaded' COMMENT '存储状态 - uploaded/processing/completed/failed',
    uploader_id VARCHAR(64) NOT NULL COMMENT '上传者用户ID',
    uploader_name VARCHAR(100) NOT NULL COMMENT '上传者名称',
    upload_time DATETIME NOT NULL COMMENT '上传时间',
    
    -- 系统字段
    create_by VARCHAR(64) COMMENT '创建者ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    remark VARCHAR(500) COMMENT '备注',
    
    KEY idx_submission_id (submission_id),
    KEY idx_file_category (file_category),
    KEY idx_upload_time (upload_time),
    KEY idx_storage_status (storage_status),
    FOREIGN KEY (submission_id) REFERENCES process_submissions(submission_id) ON DELETE CASCADE
) COMMENT='过程系统提交物文件表';

-- 3. 合同模板表
CREATE TABLE IF NOT EXISTS project_submission_demo_contract (
    submission_id BIGINT PRIMARY KEY COMMENT '提交物唯一标识',
    application_id BIGINT NOT NULL COMMENT '外键 - 关联申报记录',
    submission_type VARCHAR(30) NOT NULL DEFAULT 'contract_template' COMMENT '提交物类型 - contract_template',

    -- 项目基本信息
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    applicant_name VARCHAR(100) NOT NULL COMMENT '负责人名称',

    -- 合同模板文件
    contract_template_file_id VARCHAR(200) COMMENT '合同模板文件ID',
    contract_template_file_name VARCHAR(200) COMMENT '合同模板文件名称',
    contract_template_file_size BIGINT COMMENT '合同模板文件大小(字节)',
    contract_template_file_type VARCHAR(50) COMMENT '文件类型',
    contract_template_file_url VARCHAR(500) COMMENT '合同模板访问路径',

    -- 系统字段
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    remark VARCHAR(500) COMMENT '备注',

    KEY idx_demo_contract_application_id (application_id),
    KEY idx_demo_contract_project_name (project_name),
    KEY idx_demo_contract_upload_time (upload_time)
) COMMENT='合同模板表';

-- 4. 合同提交表
CREATE TABLE IF NOT EXISTS project_submission_contract (
    submission_id BIGINT PRIMARY KEY COMMENT '提交物唯一标识',
    application_id BIGINT NOT NULL COMMENT '外键 - 关联申报记录',
    submission_type VARCHAR(30) NOT NULL DEFAULT 'signed_contract' COMMENT '提交物类型 - signed_contract',

    -- 项目基本信息
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    applicant_name VARCHAR(100) NOT NULL COMMENT '负责人名称',

    -- 已签署合同文件
    signed_contract_file_id VARCHAR(200) COMMENT '已签署合同文件ID',
    signed_contract_file_name VARCHAR(200) COMMENT '已签署合同文件名称',
    signed_contract_file_size BIGINT COMMENT '已签署合同文件大小(字节)',
    signed_contract_file_type VARCHAR(50) COMMENT '文件类型',
    signed_contract_file_url VARCHAR(500) COMMENT '已签署合同访问路径',

    -- 合同相关信息
    contract_remarks TEXT COMMENT '合同备注',

    -- 系统字段
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    remark VARCHAR(500) COMMENT '备注',

    KEY idx_contract_application_id (application_id),
    KEY idx_contract_project_name (project_name),
    KEY idx_contract_upload_time (upload_time)
) COMMENT='合同提交表';

-- 5. 成果物提交表
CREATE TABLE IF NOT EXISTS project_submission_deliverable (
    submission_id BIGINT PRIMARY KEY COMMENT '提交物唯一标识',
    application_id BIGINT NOT NULL COMMENT '外键 - 关联申报记录',
    check_id BIGINT NOT NULL COMMENT '外键 - 关联检查记录',
    submission_type VARCHAR(30) NOT NULL DEFAULT 'deliverable_report' COMMENT '提交物类型 - deliverable_report',

    -- 项目/检查基本信息
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    applicant_name VARCHAR(100) NOT NULL COMMENT '负责人姓名',
    check_round INT NOT NULL COMMENT '检查轮次',
    check_title VARCHAR(200) COMMENT '验收轮次名称',
    check_content TEXT COMMENT '验收成果物要求描述',

    -- 成果物信息内容
    deliverable_description TEXT COMMENT '成果物描述',
    project_progress TEXT NOT NULL COMMENT '项目进展描述',
    major_achievements TEXT NOT NULL COMMENT '主要成果描述',
    problems_encountered TEXT COMMENT '遇到的问题',
    solutions TEXT COMMENT '解决方案与措施',

    -- 成果物文件
    deliverable_file_id VARCHAR(200) COMMENT '成果物文件ID',
    deliverable_file_name VARCHAR(200) COMMENT '成果物文件名称',
    deliverable_file_size BIGINT COMMENT '成果物文件大小(字节)',
    deliverable_file_type VARCHAR(50) COMMENT '文件类型',
    deliverable_file_url VARCHAR(500) COMMENT '成果物访问路径',

    -- 验收报告文件
    inspection_report_file_id VARCHAR(200) COMMENT '验收报告文件ID',
    inspection_report_file_name VARCHAR(200) COMMENT '验收报告文件名称',
    inspection_report_file_size BIGINT COMMENT '验收报告文件大小(字节)',
    inspection_report_file_type VARCHAR(50) COMMENT '文件类型',
    inspection_report_file_url VARCHAR(500) COMMENT '验收报告访问路径',

    -- 系统字段
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    remark VARCHAR(500) COMMENT '备注',

    KEY idx_deliverable_application_id (application_id),
    KEY idx_deliverable_check_id (check_id),
    KEY idx_deliverable_project_name (project_name),
    KEY idx_deliverable_upload_time (upload_time)
) COMMENT='成果物提交表';

-- 6. API访问日志表
CREATE TABLE IF NOT EXISTS process_api_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    request_id VARCHAR(64) NOT NULL COMMENT '请求ID',
    api_key VARCHAR(64) NOT NULL COMMENT 'API密钥',
    method VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
    url VARCHAR(500) NOT NULL COMMENT '请求URL',
    request_body TEXT COMMENT '请求体',
    response_code INT NOT NULL COMMENT '响应状态码',
    response_body TEXT COMMENT '响应体',
    response_time INT NOT NULL COMMENT '响应时间(ms)',
    client_ip VARCHAR(45) NOT NULL COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT 'User Agent',
    request_headers TEXT COMMENT '请求头信息',
    response_headers TEXT COMMENT '响应头信息',
    error_message TEXT COMMENT '错误信息',
    operation_type VARCHAR(50) COMMENT '业务操作类型',
    operation_result VARCHAR(20) COMMENT '操作结果 - success/failed',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    KEY idx_api_key (api_key),
    KEY idx_created_at (created_at),
    KEY idx_response_code (response_code),
    KEY idx_request_id (request_id),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_result (operation_result)
) COMMENT='过程系统API访问日志表';

-- 7. 创建中期成果物视图
CREATE OR REPLACE VIEW interim_results_view AS
SELECT 
    ps.submission_id as id,
    ps.submission_id as source_ref,
    ps.application_id as project_id,
    ps.project_name,
    CONCAT('PROC', LPAD(ps.application_id, 6, '0')) as project_code,
    CASE ps.submission_stage
        WHEN 'application' THEN '申报阶段'
        ELSE ps.submission_stage
    END as project_phase,
    COALESCE(ps.project_description, ps.project_name) as name,
    CASE ps.submission_type
        WHEN 'proposal' THEN 'application'
        WHEN 'application_attachment' THEN 'other'
        ELSE 'other'
    END as type,
    CASE ps.submission_type
        WHEN 'proposal' THEN '申报书'
        WHEN 'application_attachment' THEN '其他附件'
        ELSE '其他'
    END as type_label,
    ps.project_description as description,
    COALESCE(ps.uploader_name, ps.applicant_name) as submitter,
    ps.work_unit as submitter_dept,
    ps.upload_time as submitted_at,
    ps.sync_time as synced_at,
    'process_system' as source,
    CONCAT('http://process.example.com/submissions/', ps.submission_id) as source_url,
    CASE ps.category_level
        WHEN '重点' THEN JSON_ARRAY('重点项目', COALESCE(ps.project_field, ''))
        WHEN '一般' THEN JSON_ARRAY('一般项目', COALESCE(ps.project_field, ''))
        ELSE JSON_ARRAY(COALESCE(ps.project_field, ''))
    END as tags,
    'synced' as status,
    -- 附件信息聚合（包含申报书和其他附件）
    JSON_MERGE_PRESERVE(
        -- 申报书文件
        JSON_ARRAY(
            JSON_OBJECT(
                'id', ps.proposal_file_id,
                'name', ps.proposal_file_name,
                'url', ps.proposal_file_url,
                'size', ps.proposal_file_size,
                'ext', SUBSTRING_INDEX(ps.proposal_file_name, '.', -1),
                'category', 'proposal'
            )
        ),
        -- 其他附件
        COALESCE(ps.other_attachments_json, JSON_ARRAY())
    ) as attachments_json,
    -- 统计字段
    YEAR(ps.upload_time) as upload_year,
    ps.category_level,
    ps.project_field
FROM process_submissions ps
WHERE ps.del_flag = '0'

UNION ALL

SELECT
    dc.submission_id as id,
    dc.submission_id as source_ref,
    dc.application_id as project_id,
    dc.project_name,
    CONCAT('PROC', LPAD(dc.application_id, 6, '0')) as project_code,
    '合同阶段' as project_phase,
    CONCAT(dc.project_name, '合同模板') as name,
    'contract_template' as type,
    '合同模板' as type_label,
    dc.contract_template_file_name as description,
    dc.applicant_name as submitter,
    NULL as submitter_dept,
    dc.upload_time as submitted_at,
    NULL as synced_at,
    'contract_template' as source,
    NULL as source_url,
    JSON_ARRAY('合同模板') as tags,
    'synced' as status,
    CASE
        WHEN dc.contract_template_file_id IS NOT NULL THEN JSON_ARRAY(
            JSON_OBJECT(
                'id', dc.contract_template_file_id,
                'name', dc.contract_template_file_name,
                'url', dc.contract_template_file_url,
                'size', dc.contract_template_file_size,
                'ext', SUBSTRING_INDEX(dc.contract_template_file_name, '.', -1),
                'category', 'contract_template'
            )
        )
        ELSE JSON_ARRAY()
    END as attachments_json,
    YEAR(dc.upload_time) as upload_year,
    NULL as category_level,
    NULL as project_field
FROM project_submission_demo_contract dc
WHERE dc.del_flag = '0'

UNION ALL

SELECT
    sc.submission_id as id,
    sc.submission_id as source_ref,
    sc.application_id as project_id,
    sc.project_name,
    CONCAT('PROC', LPAD(sc.application_id, 6, '0')) as project_code,
    '合同阶段' as project_phase,
    CONCAT(sc.project_name, '已签署合同') as name,
    'signed_contract' as type,
    '已签署合同' as type_label,
    sc.contract_remarks as description,
    sc.applicant_name as submitter,
    NULL as submitter_dept,
    sc.upload_time as submitted_at,
    NULL as synced_at,
    'signed_contract' as source,
    NULL as source_url,
    JSON_ARRAY('已签署合同') as tags,
    'synced' as status,
    CASE
        WHEN sc.signed_contract_file_id IS NOT NULL THEN JSON_ARRAY(
            JSON_OBJECT(
                'id', sc.signed_contract_file_id,
                'name', sc.signed_contract_file_name,
                'url', sc.signed_contract_file_url,
                'size', sc.signed_contract_file_size,
                'ext', SUBSTRING_INDEX(sc.signed_contract_file_name, '.', -1),
                'category', 'signed_contract'
            )
        )
        ELSE JSON_ARRAY()
    END as attachments_json,
    YEAR(sc.upload_time) as upload_year,
    NULL as category_level,
    NULL as project_field
FROM project_submission_contract sc
WHERE sc.del_flag = '0'

UNION ALL

SELECT
    dr.submission_id as id,
    dr.submission_id as source_ref,
    dr.application_id as project_id,
    dr.project_name,
    CONCAT('PROC', LPAD(dr.application_id, 6, '0')) as project_code,
    '验收阶段' as project_phase,
    COALESCE(dr.check_title, '成果物报告') as name,
    'deliverable_report' as type,
    '成果物报告' as type_label,
    COALESCE(dr.deliverable_description, dr.major_achievements, dr.project_progress, dr.check_content) as description,
    dr.applicant_name as submitter,
    NULL as submitter_dept,
    dr.upload_time as submitted_at,
    NULL as synced_at,
    'deliverable_report' as source,
    NULL as source_url,
    JSON_ARRAY(
        '成果物报告',
        CASE
            WHEN dr.check_round = 0 THEN '最终验收'
            ELSE CONCAT('第', dr.check_round, '次验收')
        END
    ) as tags,
    'synced' as status,
    JSON_MERGE_PRESERVE(
        CASE
            WHEN dr.deliverable_file_id IS NOT NULL THEN JSON_ARRAY(
                JSON_OBJECT(
                    'id', dr.deliverable_file_id,
                    'name', dr.deliverable_file_name,
                    'url', dr.deliverable_file_url,
                    'size', dr.deliverable_file_size,
                    'ext', SUBSTRING_INDEX(dr.deliverable_file_name, '.', -1),
                    'category', 'deliverable'
                )
            )
            ELSE JSON_ARRAY()
        END,
        CASE
            WHEN dr.inspection_report_file_id IS NOT NULL THEN JSON_ARRAY(
                JSON_OBJECT(
                    'id', dr.inspection_report_file_id,
                    'name', dr.inspection_report_file_name,
                    'url', dr.inspection_report_file_url,
                    'size', dr.inspection_report_file_size,
                    'ext', SUBSTRING_INDEX(dr.inspection_report_file_name, '.', -1),
                    'category', 'inspection_report'
                )
            )
            ELSE JSON_ARRAY()
        END
    ) as attachments_json,
    YEAR(dr.upload_time) as upload_year,
    NULL as category_level,
    NULL as project_field
FROM project_submission_deliverable dr
WHERE dr.del_flag = '0';

-- 添加索引优化查询性能
CREATE INDEX idx_interim_results_project_id ON process_submissions(del_flag, application_id);
CREATE INDEX idx_interim_results_type ON process_submissions(del_flag, submission_type);
CREATE INDEX idx_interim_results_upload_time ON process_submissions(del_flag, upload_time);
