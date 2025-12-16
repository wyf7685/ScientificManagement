// 基础模拟数据，用于前端演示
const nowYear = new Date().getFullYear()

export const users = [
  {
    id: '1',
    username: 'admin',
    password: 'admin123',
    name: '管理员',
    email: 'admin@example.com',
    role: 'admin',
    department: '科研管理部',
    avatar: '',
    token: 'mock-admin-token'
  },
  {
    id: '2',
    username: 'expert',
    password: 'expert123',
    name: '张教授',
    email: 'expert@example.com',
    role: 'expert',
    department: '人工智能学院',
    avatar: '',
    token: 'mock-expert-token'
  },
  {
    id: '3',
    username: 'researcher',
    password: 'researcher123',
    name: '李研究员',
    email: 'researcher@example.com',
    role: 'researcher',
    department: '计算机科学与技术学院',
    avatar: '',
    token: 'mock-researcher-token'
  }
]

export const resultTypes = [
  {
    id: 'paper',
    name: '学术论文',
    code: 'paper',
    description: '期刊或会议发表的学术论文',
    enabled: true,
    fields: [
      { id: 'journal', name: 'journal', label: '期刊/会议名称', type: 'text', required: true, order: 1 },
      { id: 'doi', name: 'doi', label: 'DOI', type: 'text', required: false, order: 2 },
      { id: 'impact', name: 'impact', label: '影响因子', type: 'number', required: false, order: 3 },
      { id: 'publishDate', name: 'publishDate', label: '发表日期', type: 'date', required: true, order: 4 }
    ]
  },
  {
    id: 'patent',
    name: '发明专利',
    code: 'patent',
    description: '已申请或授权的发明专利',
    enabled: true,
    fields: [
      { id: 'patentNo', name: 'patentNo', label: '专利号/申请号', type: 'text', required: true, order: 1 },
      { id: 'type', name: 'type', label: '专利类型', type: 'select', options: ['发明专利', '实用新型', '外观设计'], required: true, order: 2 },
      { id: 'status', name: 'status', label: '法律状态', type: 'select', options: ['受理', '实审', '授权', '驳回'], required: true, order: 3 },
      { id: 'applyDate', name: 'applyDate', label: '申请日期', type: 'date', required: true, order: 4 }
    ]
  },
  {
    id: 'software',
    name: '软件著作权',
    code: 'software',
    description: '计算机软件著作权登记',
    enabled: true,
    fields: [
      { id: 'regNo', name: 'regNo', label: '登记号', type: 'text', required: true, order: 1 },
      { id: 'devDate', name: 'devDate', label: '开发完成日期', type: 'date', required: true, order: 2 },
      { id: 'pubDate', name: 'pubDate', label: '首次发表日期', type: 'date', required: false, order: 3 },
      { id: 'owner', name: 'owner', label: '著作权人', type: 'text', required: true, order: 4 }
    ]
  }
]

export const projects = [
  {
    id: 'p-001',
    name: '面向大规模预训练模型的参数高效微调方法研究',
    code: '62306124',
    description: '针对大模型落地难问题,研究参数高效微调算法,降低训练成本与显存占用。',
    type: '青年科学基金项目',
    field: '人工智能'
  },
  {
    id: 'p-002',
    name: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    code: '2024YFB3301200',
    description: '融合视觉、激光雷达等多源传感器数据,解决恶劣天气下的感知难题。',
    type: '国家重点研发计划',
    field: '智能交通'
  },
  {
    id: 'p-003',
    name: '高比能固态锂电池界面稳定性及调控机制',
    code: '52372056',
    description: '研究固态电解质与电极界面的失效机理,提出界面修饰新策略。',
    type: '面上项目',
    field: '新能源材料'
  },
  {
    id: 'p-004',
    name: '气候变化背景下典型湿地生态系统碳汇功能演变特征',
    code: '32370412',
    description: '监测湿地碳通量变化,揭示气候变暖对碳汇功能的影响机制。',
    type: '面上项目',
    field: '生态环境'
  },
  {
    id: 'p-005',
    name: '针对耐药菌的新型抗菌肽设计及其膜穿孔作用机理研究',
    code: '82303451',
    description: '设计新型高效低毒抗菌肽,阐明其杀菌机制,为抗耐药菌药物研发提供新思路。',
    type: '青年科学基金项目',
    field: '生物医药'
  },
  {
    id: 'p-006',
    name: '数字经济驱动下制造业供应链韧性提升策略研究',
    code: '72371089',
    description: '分析数字技术对供应链韧性的赋能作用,构建供应链风险预警与应对模型。',
    type: '面上项目',
    field: '管理科学'
  }
]

export const results = [
  {
    id: 'r-001',
    title: 'Parameter-Efficient Fine-Tuning for Large Vision Models',
    type: 'paper',
    typeId: 'paper',
    authors: ['李研究员', '张教授'],
    year: nowYear,
    abstract: '提出了一种新的适配器结构,仅需微调0.5%的参数即可在下游任务上达到全量微调的性能。',
    keywords: ['大模型', '微调', '计算机视觉'],
    projectId: 'p-001',
    projectName: '面向大规模预训练模型的参数高效微调方法研究',
    projectCode: '62306124',
    source: 'manual_upload',
    sourceStage: 'design',
    projectPhase: 'development',
    sourceRef: 'internal-upload:r-001',
    syncTime: '2024-03-10 09:00',
    status: 'published',
    visibility: 'public_full',
    attachments: [
      { id: 'att-1', name: 'Paper_Full_Text.pdf', size: 1024 * 2500, type: 'pdf', url: '/mock/paper1.pdf', uploadedAt: '2024-03-15' }
    ],
    metadata: { journal: 'CVPR 2024', doi: '10.1109/CVPR.2024.00123', impact: 10.5, publishDate: '2024-06-18' },
    createdBy: '李研究员',
    createdAt: '2024-03-10',
    updatedAt: '2024-06-20',
    reviewHistory: [
      { id: 'rev-1', reviewerId: '2', reviewerName: '张教授', action: 'approve', comment: '该成果创新性突出,实验数据详实,建议公开发表。', createdAt: '2024-03-12' }
    ]
  },
  {
    id: 'r-002',
    title: '一种基于多传感器融合的车辆目标检测方法及系统',
    type: 'patent',
    typeId: 'patent',
    authors: ['李研究员', '王博士'],
    year: nowYear,
    abstract: '本发明公开了一种车辆检测方法,通过融合毫米波雷达与摄像头数据,提高了雨雾天气的检测精度。',
    keywords: ['自动驾驶', '目标检测', '传感器融合'],
    projectId: 'p-002',
    projectName: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    projectCode: '2024YFB3301200',
    source: 'process_system',
    sourceStage: 'experiment',
    projectPhase: 'experiment',
    sourceRef: 'process-system:exp-2024-05-20',
    syncTime: '2024-05-20 10:30',
    status: 'reviewing',
    assignedReviewers: ['张教授'],
    visibility: 'internal_full',
    attachments: [
      { id: 'att-2', name: '专利受理通知书.pdf', size: 1024 * 500, type: 'pdf', url: '/mock/patent_notice.pdf', uploadedAt: '2024-05-20' }
    ],
    metadata: { patentNo: '202410123456.X', type: '发明专利', status: '实审', applyDate: '2024-04-10' },
    createdBy: '李研究员',
    createdAt: '2024-04-15',
    updatedAt: '2024-05-20',
    reviewHistory: []
  },
  {
    id: 'r-003',
    title: '分布式科研数据管理系统 V1.0',
    type: 'software',
    typeId: 'software',
    authors: ['李研究员'],
    year: nowYear - 1,
    abstract: '一套支持多源异构数据接入、存储与共享的科研数据管理平台。',
    keywords: ['数据管理', '分布式系统', '科研平台'],
    projectId: 'p-006',
    projectName: '数字经济驱动下制造业供应链韧性提升策略研究',
    projectCode: '72371089',
    source: 'manual_upload',
    sourceStage: 'delivery',
    projectPhase: 'operation',
    sourceRef: 'internal-upload:r-003',
    syncTime: '2023-12-20 14:00',
    status: 'published',
    visibility: 'internal_abstract',
    attachments: [],
    metadata: { regNo: '2023SR0123456', devDate: '2023-11-30', pubDate: '2023-12-15', owner: 'XX大学' },
    createdBy: '李研究员',
    createdAt: '2023-12-20',
    updatedAt: '2024-01-05',
    reviewHistory: [
      { id: 'rev-2', reviewerId: '2', reviewerName: '张教授', action: 'approve', comment: '符合软著登记要求,材料齐全。', createdAt: '2023-12-25' }
    ]
  },
  {
    id: 'r-004',
    title: '多模态交通场景数据集 V2.0',
    type: 'paper',
    typeId: 'paper',
    authors: ['过程系统'],
    year: nowYear,
    abstract: '自动采集并生成的交通多模态数据集，包含雨雾夜等复杂场景标签。',
    keywords: ['多模态', '交通', '数据集'],
    projectId: 'p-002',
    projectName: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    projectCode: '2024YFB3301200',
    source: 'process_system',
    sourceStage: 'data_collection',
    projectPhase: 'development',
    sourceRef: 'process-system:dataset-2025-01',
    syncTime: '2025-01-12 11:00',
    status: 'pending',
    assignedReviewers: [],
    visibility: 'internal_full',
    attachments: [],
    metadata: { journal: '内部数据产出', doi: '', impact: null, publishDate: '' },
    createdBy: '系统同步',
    createdAt: '2025-01-12',
    updatedAt: '2025-01-12',
    reviewHistory: []
  },
  {
    id: 'r-005',
    title: '新能源固态电解质界面改性实验报告',
    type: 'paper',
    typeId: 'paper',
    authors: ['李研究员'],
    year: nowYear,
    abstract: '描述固态电解质与电极界面改性实验方案与阶段结果，需补充数据。',
    keywords: ['固态电解质', '界面改性', '锂电池'],
    projectId: 'p-003',
    projectName: '高比能固态锂电池界面稳定性及调控机制',
    projectCode: '52372056',
    source: 'manual_upload',
    sourceStage: 'experiment',
    projectPhase: 'experiment',
    sourceRef: 'internal-upload:r-005',
    syncTime: '2024-12-30 18:00',
    status: 'revision',
    assignedReviewers: ['王博士'],
    visibility: 'internal_abstract',
    attachments: [],
    metadata: { journal: '内部评审稿', doi: '', impact: null, publishDate: '' },
    createdBy: '李研究员',
    createdAt: '2024-12-30',
    updatedAt: '2025-01-02',
    reviewHistory: [
      { id: 'rev-5', reviewerId: '2', reviewerName: '张教授', action: 'request_changes', comment: '需要补充实验对照组数据。', createdAt: '2025-01-02' }
    ]
  },
  {
    id: 'r-006',
    title: '湿地生态系统碳汇功能评估报告',
    type: 'paper',
    typeId: 'paper',
    authors: ['李研究员'],
    year: nowYear,
    abstract: '基于三年连续监测数据,评估了本省主要湿地的碳汇潜力,并提出保护建议。',
    keywords: ['碳汇', '湿地', '生态评估'],
    projectId: 'p-004',
    projectName: '气候变化背景下典型湿地生态系统碳汇功能演变特征与驱动机制',
    projectCode: '32370412',
    source: 'manual_upload',
    sourceStage: 'experiment',
    projectPhase: 'experiment',
    status: 'rejected',
    visibility: 'private',
    attachments: [],
    metadata: { journal: '内部呈阅件', doi: '', impact: null, publishDate: '2024-07-01' },
    createdBy: '李研究员',
    createdAt: '2024-07-05',
    updatedAt: '2024-07-10',
    reviewHistory: [
      { id: 'rev-4', reviewerId: '2', reviewerName: '张教授', action: 'reject', comment: '数据分析部分存在逻辑漏洞,建议补充最新的监测数据后重新提交。', createdAt: '2024-07-08' }
    ]
  }
]

const baseYears = Array.from({ length: 5 }, (_, idx) => (nowYear - 4 + idx).toString())

export const dashboardAnalytics = {
  distribution: {
    type: [
      { name: '学术论文', value: 18 },
      { name: '发明专利', value: 9 },
      { name: '软件著作权', value: 6 }
    ],
    indexLevel: [
      { name: 'SCI Q1', value: 7 },
      { name: 'SCI Q2', value: 5 },
      { name: 'CCF A', value: 4 },
      { name: 'CCF B', value: 3 },
      { name: 'EI', value: 2 }
    ],
    department: [
      { name: '人工智能学院', value: 10 },
      { name: '电子信息学院', value: 7 },
      { name: '生物医药学院', value: 6 },
      { name: '材料科学学院', value: 4 }
    ],
    team: [
      { name: '视觉与多模态组', value: 5 },
      { name: '传感融合与控制组', value: 4 },
      { name: 'AIDD 计算组', value: 3 },
      { name: '新能源接口组', value: 3 },
      { name: '供应链数智化组', value: 3 }
    ]
  },
  stackedTrend: {
    type: {
      timeline: baseYears,
      stacks: [
        { key: 'paper', name: '学术论文', data: [10, 13, 17, 19, 22] },
        { key: 'patent', name: '发明专利', data: [4, 5, 6, 7, 8] },
        { key: 'software', name: '软件著作权', data: [2, 3, 4, 5, 6] }
      ],
      citations: [120, 180, 260, 340, 430]
    },
    indexLevel: {
      timeline: baseYears,
      stacks: [
        { key: 'SCI Q1', name: 'SCI Q1', data: [3, 4, 5, 6, 7] },
        { key: 'SCI Q2', name: 'SCI Q2', data: [2, 3, 3, 4, 5] },
        { key: 'CCF A', name: 'CCF A', data: [2, 2, 3, 3, 3] },
        { key: 'CCF B', name: 'CCF B', data: [1, 2, 2, 3, 3] },
        { key: 'EI', name: 'EI', data: [1, 1, 2, 2, 2] }
      ],
      citations: [110, 150, 210, 290, 360]
    },
    department: {
      timeline: baseYears,
      stacks: [
        { key: '人工智能学院', name: '人工智能学院', data: [5, 6, 7, 8, 9] },
        { key: '电子信息学院', name: '电子信息学院', data: [3, 4, 5, 5, 6] },
        { key: '生物医药学院', name: '生物医药学院', data: [2, 3, 4, 5, 5] },
        { key: '材料科学学院', name: '材料科学学院', data: [1, 2, 2, 3, 3] }
      ],
      citations: [90, 140, 200, 280, 350]
    },
    team: {
      timeline: baseYears,
      stacks: [
        { key: '视觉与多模态组', name: '视觉与多模态组', data: [2, 3, 4, 5, 5] },
        { key: '传感融合与控制组', name: '传感融合与控制组', data: [2, 2, 3, 4, 4] },
        { key: 'AIDD 计算组', name: 'AIDD 计算组', data: [1, 2, 2, 3, 3] },
        { key: '新能源接口组', name: '新能源接口组', data: [1, 1, 2, 2, 2] },
        { key: '供应链数智化组', name: '供应链数智化组', data: [1, 1, 1, 2, 3] }
      ],
      citations: [60, 90, 140, 200, 260]
    }
  },
  keywordGraph: {
    nodes: [
      { name: 'Large Vision Models', value: 42, category: 'AI' },
      { name: 'Multimodal Fusion', value: 32, category: 'AI' },
      { name: 'Sensor Fusion', value: 28, category: 'Smart Mobility' },
      { name: 'Reinforcement Learning', value: 21, category: 'AI' },
      { name: 'Edge Computing', value: 18, category: 'Systems' },
      { name: 'AIDD', value: 24, category: 'BioMed' },
      { name: 'Protein Design', value: 16, category: 'BioMed' },
      { name: 'Graph Learning', value: 17, category: 'AI' },
      { name: 'Power Battery', value: 14, category: 'Energy' },
      { name: 'Solid Electrolyte', value: 12, category: 'Energy' },
      { name: 'Supply Chain Resilience', value: 15, category: 'Management' },
      { name: 'Demand Forecast', value: 13, category: 'Management' }
    ],
    links: [
      { source: 'Large Vision Models', target: 'Multimodal Fusion', value: 5 },
      { source: 'Large Vision Models', target: 'Graph Learning', value: 3 },
      { source: 'Multimodal Fusion', target: 'Sensor Fusion', value: 4 },
      { source: 'Sensor Fusion', target: 'Reinforcement Learning', value: 2 },
      { source: 'AIDD', target: 'Protein Design', value: 4 },
      { source: 'AIDD', target: 'Graph Learning', value: 2 },
      { source: 'Power Battery', target: 'Solid Electrolyte', value: 3 },
      { source: 'Supply Chain Resilience', target: 'Demand Forecast', value: 4 },
      { source: 'Edge Computing', target: 'Sensor Fusion', value: 2 }
    ]
  }
}

export const uploads = []

export const demands = [
  {
    id: 'd-001',
    title: '三甲医院希望提升放射科影像诊断效率，减少人工标注负担',
    summary: '某三甲医院计划在放射科引入智能影像辅助诊断，提高分割与检出效率，并降低医师重复工作量。',
    llmSummary: '医院需要引入医疗影像AI，重点在分割、检测、效率提升，关注合规与部署。',
    keywords: ['医疗影像', 'AI分割', '效率'],
    tags: ['医疗', '效率提升'],
    industry: '医疗健康',
    region: '北京',
    sourceCategory: '政府/园区平台',
    sourceSite: 'HospitalTech Forum',
    sourceUrl: 'https://example.com/hospital/need-1',
    capturedAt: '2025-01-12',
    confidence: 0.82,
    status: 'matched',
    bestMatchScore: 0.86,
    matches: [
      {
        resultId: 'r-001',
        resultTitle: '深度学习在医学影像中的应用',
        resultType: '论文',
        owner: '科研用户',
        matchScore: 0.86,
        reason: '需求中的“影像分割”“诊断效率”与成果摘要高相似度，关键词重叠。',
        sourceSnippet: '希望在放射科引入智能影像分割与检测，减少医生重复标注工作。',
        updatedAt: '2025-01-13'
      }
    ]
  },
  {
    id: 'd-002',
    title: '温室作物生长环境智能调控与病虫害预警技术',
    summary: '开发基于物联网的温室环境监测系统,利用多传感器融合技术实时采集温湿度、光照等数据,并建立病虫害预测模型,实现精准施药决策。',
    llmSummary: '智慧农业需求:IoT环境监测+病虫害AI预警,目标是精准施药与环境调控。',
    keywords: ['物联网', '智慧农业', '病虫害预警', '环境控制'],
    tags: ['现代农业', '物联网'],
    industry: '现代农业',
    region: '山东',
    sourceCategory: '农业科技推广平台',
    sourceSite: 'AgriTech China',
    sourceUrl: 'https://example.com/demand/002',
    capturedAt: '2025-01-10',
    confidence: 0.85,
    status: 'unmatched',
    bestMatchScore: 0.45,
    matches: []
  },
  {
    id: 'd-003',
    title: '基于AI的创新药物靶点发现与高通量筛选平台',
    summary: '利用人工智能技术分析海量生物医学数据,挖掘潜在的药物新靶点,并构建虚拟筛选模型,缩短新药研发周期,降低研发成本。',
    llmSummary: '生物医药需求:AI辅助药物研发(AIDD),聚焦靶点发现与高通量筛选。',
    keywords: ['AI for Science', '药物研发', '生物信息学', '高通量筛选'],
    tags: ['生物医药', '人工智能'],
    industry: '生物医药',
    region: '上海',
    sourceCategory: '张江药谷需求发布',
    sourceSite: 'Zhangjiang Pharma',
    sourceUrl: 'https://example.com/demand/003',
    capturedAt: '2025-01-08',
    confidence: 0.88,
    status: 'matched',
    bestMatchScore: 0.75,
    matches: [
      {
        resultId: 'r-001',
        resultTitle: 'Parameter-Efficient Fine-Tuning for Large Vision Models',
        resultType: '学术论文',
        owner: '李研究员',
        matchScore: 0.65,
        reason: '虽然主要是视觉模型,但微调技术可借鉴用于生物序列大模型,存在一定技术关联。',
        sourceSnippet: '利用人工智能技术分析海量生物医学数据...',
        updatedAt: '2025-01-09'
      }
    ]
  },
  {
    id: 'd-004',
    title: '城市复杂交通场景下的多源数据融合与拥堵治理',
    summary: '整合交通摄像头、浮动车数据、地磁感应等多源异构数据,构建城市交通态势感知平台,提出基于强化学习的信号灯优化控制策略。',
    llmSummary: '智慧交通需求:多源数据融合+强化学习信控,解决城市拥堵问题。',
    keywords: ['智能交通', '数据融合', '交通优化', '强化学习'],
    tags: ['智慧城市', '交通'],
    industry: '智慧城市',
    region: '浙江',
    sourceCategory: '政府采购网',
    sourceSite: 'Zhejiang Gov Procurement',
    sourceUrl: 'https://example.com/demand/004',
    capturedAt: '2025-01-15',
    confidence: 0.95,
    status: 'matched',
    bestMatchScore: 0.92,
    matches: [
      {
        resultId: 'r-002',
        resultTitle: '一种基于多传感器融合的车辆目标检测方法及系统',
        resultType: '发明专利',
        owner: '李研究员',
        matchScore: 0.92,
        reason: '专利中的多传感器融合技术直接对应需求中的"多源数据融合",且应用场景高度一致。',
        sourceSnippet: '整合交通摄像头...多源异构数据...',
        updatedAt: '2025-01-16'
      }
    ]
  },
  {
    id: 'd-005',
    title: '耐高温高强韧轻质合金材料研发及成型工艺',
    summary: '面向航空航天领域需求,研发新型耐高温(≥800℃)轻质合金材料,突破材料成分设计与精密铸造工艺瓶颈,提升构件的疲劳寿命。',
    llmSummary: '新材料需求:航空航天用耐高温轻质合金,需解决成分设计与铸造工艺难题。',
    keywords: ['先进材料', '轻质合金', '耐高温', '精密铸造'],
    tags: ['新材料', '航空航天'],
    industry: '新材料',
    region: '陕西',
    sourceCategory: '军民融合平台',
    sourceSite: 'Civil-Military Integration',
    sourceUrl: 'https://example.com/demand/005',
    capturedAt: '2025-01-05',
    confidence: 0.80,
    status: 'unmatched',
    bestMatchScore: 0.40,
    matches: []
  }
]

export const crawlerSources = [
  {
    id: 'cs-001',
    name: '政府采购网需求',
    type: 'html',
    industry: '智慧城市',
    region: '全国',
    baseUrl: 'https://www.ccgp.gov.cn',
    description: '抓取全国政府采购网发布的产业需求信息',
    authType: 'none',
    credentials: {},
    frequencyHours: 6,
    priority: 'high',
    tags: ['政府', '交通', '能源'],
    enabled: true,
    lastRunAt: '2025-01-15 09:00',
    lastSuccessAt: '2025-01-15 09:00',
    status: 'healthy'
  },
  {
    id: 'cs-002',
    name: '张江药谷发布',
    type: 'rss',
    industry: '生物医药',
    region: '上海',
    baseUrl: 'https://pharma.zhangjiang.com/rss',
    description: '对接药谷 RSS，关注医药类合作需求',
    authType: 'api_key',
    credentials: {
      apiKey: 'mock-key'
    },
    frequencyHours: 12,
    priority: 'medium',
    tags: ['医药', '合作', 'AIDD'],
    enabled: true,
    lastRunAt: '2025-01-14 18:00',
    lastSuccessAt: '2025-01-14 18:00',
    status: 'warning',
    failureReason: '部分条目格式异常'
  },
  {
    id: 'cs-003',
    name: '产业园区申报系统',
    type: 'api',
    industry: '新能源',
    region: '江苏',
    baseUrl: 'https://api.industrial-park.cn/demand',
    description: '企业主动填报需求的对接 API',
    authType: 'basic',
    credentials: {
      username: 'demo',
      password: 'demo'
    },
    frequencyHours: 24,
    priority: 'low',
    tags: ['新能源', '碳中和'],
    enabled: false,
    lastRunAt: '2025-01-10 09:00',
    lastSuccessAt: '2025-01-10 09:00',
    status: 'idle'
  }
]

export const crawlerSettings = {
  defaultFrequencyHours: 8,
  retryLimit: 3,
  autoTagging: true,
  deduplicateThreshold: 0.78,
  notifyEmails: ['ops@labnexus.cn', 'pm@labnexus.cn'],
  notifyWebhook: ''
}

export function nextId(prefix = 'r') {
  return `${prefix}-${Math.random().toString(16).slice(2, 8)}`
}
