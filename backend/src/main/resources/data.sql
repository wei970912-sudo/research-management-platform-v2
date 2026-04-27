INSERT IGNORE INTO rm_project (id, name, owner, status, current_stage, milestone_completion_rate, description) VALUES
('P-1001', '机场行李处理系统预测性运维关键技术研究', '项目办公室', '进行中', '核心功能开发', 72, '聚焦机场行李处理系统预测性运维的关键技术研究与验证。'),
('P-1002', '航空货运智能组板机器人', '科研团队', '验收准备', '验收交付', 83, '围绕机器人交付、合同执行与验收材料归档开展管理。'),
('P-1003', '科研管理平台 V2', '平台团队', '规划中', '需求确认', 35, '将旧版静态科研管理工具升级为前后端分离、具备统一数据库底座和本地 AI 助手的平台。');

INSERT IGNORE INTO rm_phase (id, project_id, name, owner, start_date, end_date, status) VALUES
('PH-1001', 'P-1001', '需求与方案规划', '项目办公室', '2026-04-01', '2026-04-30', '已完成'),
('PH-1002', 'P-1001', '核心功能开发', '研发团队', '2026-05-01', '2026-07-20', '进行中'),
('PH-1003', 'P-1002', '系统联调测试', '测试团队', '2026-04-10', '2026-06-10', '进行中'),
('PH-1004', 'P-1002', '验收交付准备', '科研团队', '2026-06-11', '2026-08-01', '未开始'),
('PH-1005', 'P-1003', '需求确认', '平台团队', '2026-04-15', '2026-05-15', '进行中'),
('PH-1006', 'P-1003', '架构设计', '平台团队', '2026-05-16', '2026-06-15', '未开始');

INSERT IGNORE INTO rm_milestone (id, project_id, phase_id, name, date, status, critical) VALUES
('M-1001', 'P-1001', 'PH-1002', '中期检查材料包', '2026-07-01', '未开始', TRUE),
('M-1002', 'P-1001', 'PH-1002', '方案评审完成', '2026-05-10', '进行中', TRUE),
('M-1003', 'P-1002', 'PH-1004', '验收会议', '2026-07-18', '未开始', TRUE),
('M-1004', 'P-1002', 'PH-1003', '联调测试报告', '2026-05-28', '进行中', FALSE),
('M-1005', 'P-1003', 'PH-1005', '架构评审', '2026-05-20', '规划中', TRUE);

INSERT IGNORE INTO rm_meeting (id, project_id, topic, date, host, type, location, attendees, summary, attachment_names) VALUES
('MT-1001', 'P-1001', '项目周例会', '2026-04-24', '项目办公室', '线上', '腾讯会议', '项目办公室,研发团队,测试团队', '跟踪阶段性交付物与关键风险。', 'weekly-sync-minutes.docx'),
('MT-1002', 'P-1002', '合同执行评审会', '2026-04-25', '合同负责人', '线下', 'A301 会议室', '合同负责人,科研团队,财务人员', '核查签约节点、预算执行和交付计划。', ''),
('MT-1003', 'P-1003', 'V2 架构工作坊', '2026-04-26', '平台团队', '线上', '飞书会议', '平台团队,产品团队', '确认统一数据库底座与 AI 助手设计。', 'architecture-workshop.pdf');

INSERT IGNORE INTO rm_contract (id, project_id, name, amount, status, task_name, task_budget, signing_unit, signing_date, delivery_date, content, delivery_terms, payment_terms) VALUES
('CT-1001', 'P-1002', '算法开发合同', 280000.00, '已签订', '智能组板算法开发', 350000.00, '华东智能技术有限公司', '2026-04-20', '2026-07-30', '完成算法开发与系统集成支持。', '交付源码包、部署说明与测试报告。', '签约后支付 30%，验收后支付 70%。'),
('CT-1002', 'P-1003', '平台交付合同', 120000.00, '待签订', '平台前后端改造', 180000.00, '平台交付供应商', '2026-05-10', '2026-08-15', '交付重构后的前后端分离平台。', '交付可部署版本、接口文档与迁移说明。', '项目启动支付 50%，交付完成支付 50%。');

INSERT IGNORE INTO rm_document (id, project_id, node_name, attachment_count, status, owner, target_date, description, notes, attachment_names) VALUES
('DOC-1001', 'P-1001', '中期检查归档', 6, '进行中', '项目负责人', '2026-06-30', '归档中期检查报告、测试记录与过程材料。', '待补充签章版材料。', 'midterm-report.docx,test-record.xlsx'),
('DOC-1002', 'P-1002', '验收归档', 12, '已归档', '资料管理员', '2026-08-01', '归档验收报告、交付清单、合同材料与现场照片。', '已完成归档。', 'acceptance-report.pdf,delivery-list.xlsx'),
('DOC-1003', 'P-1003', '架构设计资料', 4, '进行中', '平台团队', '2026-05-30', '整理架构说明、接口清单和迁移说明。', '持续补充设计材料。', 'architecture.docx,api-list.xlsx');

INSERT IGNORE INTO rm_calendar_event (id, project_id, title, date, time, type, remind_before, note) VALUES
('EV-1001', 'P-1001', '项目周例会', '2026-04-24', '09:30', '会议提醒', 0, '项目周例会'),
('EV-1002', 'P-1002', '验收材料预审', '2026-05-06', '14:00', '材料提交', 3, '验收材料预审安排'),
('EV-1003', 'P-1003', '架构评审', '2026-05-20', '10:00', '关键节点', 7, '架构评审会议');
