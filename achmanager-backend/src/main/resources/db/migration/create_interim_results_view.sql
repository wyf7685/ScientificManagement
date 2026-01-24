-- 创建中期成果物视图
-- 用于管理员界面查询和展示过程系统同步的数据

CREATE OR REPLACE VIEW interim_results_view AS
SELECT 
    ps.submission_id as id,
    ps.submission_id as source_ref,
    ps.application_id as project_id,
    ps.project_name,
    CONCAT('PROC', LPAD(ps.application_id, 6, '0')) as project_code,
    CASE ps.submission_stage
        WHEN 'application' THEN '申报阶段'
        WHEN 'review' THEN '评审阶段'
        WHEN 'execution' THEN '执行阶段'
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

-- 创建索引以优化查询性能
CREATE INDEX idx_interim_results_project_id ON process_submissions(del_flag, application_id);
CREATE INDEX idx_interim_results_type ON process_submissions(del_flag, submission_type);
CREATE INDEX idx_interim_results_upload_time ON process_submissions(del_flag, upload_time);
CREATE INDEX idx_interim_results_category ON process_submissions(del_flag, category_level);
CREATE INDEX idx_interim_results_field ON process_submissions(del_flag, project_field);
CREATE INDEX idx_interim_results_submitter ON process_submissions(del_flag, applicant_name);
