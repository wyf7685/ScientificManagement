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
    formatChecked: true,
    formatStatus: 'passed',
    formatNote: '',
    status: 'published',
    visibility: 'public_full',
    permissionStatus: 'full',
    accessRequestStatus: 'approved',
    canRequestAccess: false,
    lastRequestAt: '2024-03-08 10:00',
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
    formatChecked: false,
    formatStatus: 'pending',
    formatNote: '',
    status: 'reviewing',
    assignedReviewers: ['张教授'],
    visibility: 'internal_full',
    permissionStatus: 'summary',
    accessRequestStatus: 'pending',
    canRequestAccess: false,
    lastRequestAt: '2025-01-08 14:30',
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
    formatChecked: true,
    formatStatus: 'passed',
    formatNote: '',
    status: 'published',
    visibility: 'internal_abstract',
    permissionStatus: 'summary',
    accessRequestStatus: 'pending',
    canRequestAccess: false,
    lastRequestAt: '2025-01-05 11:20',
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
    formatChecked: false,
    formatStatus: 'failed',
    formatNote: '缺少数据来源说明与字段定义',
    status: 'pending',
    assignedReviewers: [],
    visibility: 'internal_full',
    permissionStatus: 'summary',
    accessRequestStatus: 'approved',
    canRequestAccess: false,
    lastRequestAt: '2024-12-28 16:20',
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
    formatChecked: true,
    formatStatus: 'passed',
    formatNote: '',
    status: 'revision',
    assignedReviewers: ['王博士'],
    visibility: 'internal_abstract',
    permissionStatus: 'summary',
    accessRequestStatus: 'rejected',
    canRequestAccess: true,
    rejectedReason: '申请理由过于宽泛，请说明使用场景与保护措施。',
    lastRequestAt: '2025-01-02 09:10',
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
    permissionStatus: 'denied',
    accessRequestStatus: 'none',
    canRequestAccess: true,
    attachments: [],
    metadata: { journal: '内部呈阅件', doi: '', impact: null, publishDate: '2024-07-01' },
    createdBy: '李研究员',
    createdAt: '2024-07-05',
    updatedAt: '2024-07-10',
    reviewHistory: [
      { id: 'rev-4', reviewerId: '2', reviewerName: '张教授', action: 'reject', comment: '数据分析部分存在逻辑漏洞,建议补充最新的监测数据后重新提交。', createdAt: '2024-07-08' }
    ]
  },
  {
    id: 'r-007',
    title: 'Attention Is All You Need: 基于自注意力机制的Transformer架构',
    type: 'paper',
    typeId: 'paper',
    authors: ['Ashish Vaswani', 'Noam Shazeer', 'Niki Parmar', '张教授', '李研究员'],
    year: 2017,
    abstract: '本文提出了一种革命性的神经网络架构Transformer,完全摒弃了传统的循环神经网络(RNN)和卷积神经网络(CNN)结构,仅依赖自注意力机制(Self-Attention Mechanism)来建模序列之间的全局依赖关系。',
    content: `【摘要】本文提出了一种革命性的神经网络架构Transformer,完全摒弃了传统的循环神经网络(RNN)和卷积神经网络(CNN)结构,仅依赖自注意力机制(Self-Attention Mechanism)来建模序列之间的全局依赖关系。

【背景与动机】传统的序列转换模型主要基于复杂的循环或卷积神经网络,通常包含编码器和解码器。表现最优的模型还通过注意力机制连接编码器和解码器。然而,这些模型存在固有的顺序计算特性,限制了训练时的并行化能力,在处理长序列时尤为明显。虽然有研究通过因子分解技巧和条件计算来提升计算效率,但顺序计算的根本限制依然存在。注意力机制已成为序列建模的重要组成部分,但此前主要与循环网络配合使用。

【核心架构】Transformer架构由编码器(Encoder)和解码器(Decoder)两部分组成,每部分均由6层相同的层堆叠而成。编码器的每一层包含两个子层:第一个是多头自注意力机制,第二个是简单的位置全连接前馈网络。解码器在这两个子层之间插入了第三个子层,对编码器栈的输出执行多头注意力。所有子层周围都采用残差连接(Residual Connection),随后进行层归一化(Layer Normalization)。

【注意力机制详解】本文定义的注意力函数将查询(Query)和一组键值对(Key-Value Pairs)映射到输出,其中查询、键、值和输出都是向量。输出是值的加权和,其中分配给每个值的权重由查询与相应键的相似度函数计算得出。我们称这种特定的注意力为"Scaled Dot-Product Attention"。输入包括维度为dk的查询和键,以及维度为dv的值。我们计算查询与所有键的点积,除以√dk,然后应用softmax函数来获得值的权重。

【多头注意力】多头注意力机制允许模型同时关注来自不同位置的不同表示子空间的信息。具体而言,我们将查询、键和值分别通过h个不同的学习线性投影到dk、dk和dv维度。然后在每个投影版本的查询、键和值上并行执行注意力函数,产生dv维输出值。这些值被连接并再次投影,得到最终值。本文采用h=8个并行注意力层或头。对于每一个,我们使用dk=dv=dmodel/h=64。由于每个头的维度减小,总计算成本与完整维度的单头注意力相似。

【位置编码】由于模型不包含循环和卷积,为了让模型利用序列的顺序信息,必须注入序列中标记的相对或绝对位置信息。为此,我们在编码器和解码器栈底部的输入嵌入中添加"位置编码"。位置编码具有与嵌入相同的维度dmodel,因此两者可以相加。本文使用不同频率的正弦和余弦函数作为位置编码,这使得模型可以轻松学习关注相对位置。

【实验结果】在机器翻译任务上,我们在WMT 2014英语到德语翻译任务上训练了模型,该任务包含约450万个句子对。我们的big transformer模型在测试集上达到了28.4的BLEU分数,超越了所有之前报告的单模型,建立了新的最先进水平,同时训练成本仅为之前最佳模型的一小部分。在WMT 2014英语到法语翻译任务上,我们的big模型达到了41.8的BLEU分数,超越了所有之前发布的单模型,训练成本不到之前最先进模型的1/4。

我们还在8个P100 GPU上训练了base模型,每个训练步骤约需0.4秒。我们训练base模型共100,000步或12小时。对于big模型,步骤时间为1.0秒。Big模型训练了300,000步(3.5天)。为了评估Transformer不同组件的重要性,我们以不同方式改变了base模型,测量了开发集newstest2013上英德翻译性能的变化。消融实验表明,多头注意力对性能至关重要,减少注意力头的数量会降低性能。

【模型变体分析】我们通过改变注意力头数量和注意力键与值的维度进行了实验,保持计算量恒定。单头注意力比最佳设置差0.9 BLEU,头太多也会降低质量。减小注意力键的大小dk会损害模型质量,这表明确定兼容性并不容易,可能比点积更复杂的兼容性函数是有益的。

【泛化能力验证】为了评估Transformer是否能泛化到其他任务,我们对英语成分句法分析进行了实验。这项任务存在特定挑战:输出受到强结构约束且显著长于输入。此外,RNN序列到序列模型在小数据环境下无法取得最先进结果。在WSJ仅训练40K句子的情况下,我们的模型在WSJ第23节测试集上达到了91.3的F1分数,超越了除循环神经网络语法之外的所有先前报告模型。

【训练细节】我们使用Adam优化器,β1=0.9,β2=0.98,ε=10^-9。我们根据公式改变学习率:lrate = d^(-0.5)_model · min(step_num^(-0.5), step_num · warmup_steps^(-1.5))。这对应于在第一个warmup_steps训练步骤中线性增加学习率,之后与步数的平方根成反比地减少。我们使用warmup_steps=4000。在训练期间,我们采用了三种类型的正则化:残差Dropout、标签平滑等技术。

【结论与展望】在本工作中,我们提出了Transformer,这是第一个完全基于注意力的序列转换模型,用多头自注意力取代了编码器-解码器架构中最常用的循环层。对于翻译任务,Transformer的训练速度显著快于基于循环或卷积层的架构。在WMT 2014英德和英法翻译任务上,我们达到了新的最先进水平。前者我们的最佳模型甚至超越了所有先前报告的集成模型。我们对基于注意力的模型的未来感到兴奋,并计划将它们应用于其他任务。我们计划将Transformer扩展到涉及文本以外的输入和输出模态的问题,并研究局部受限注意力机制,以有效处理图像、音频和视频等大型输入和输出。使生成更少顺序化是我们的另一个研究目标。

【影响与贡献】Transformer架构的提出标志着深度学习领域的重要里程碑。它不仅在机器翻译任务上取得了突破性成果,更重要的是为后续的BERT、GPT、T5等大规模预训练语言模型奠定了理论基础。自注意力机制的并行化特性使得模型能够有效处理超长序列,捕捉全局依赖关系,这在传统RNN架构中难以实现。多头注意力机制使模型能够从不同表示子空间学习信息,增强了模型的表达能力。位置编码的引入巧妙地解决了无序模型中的序列信息保留问题。Transformer的成功证明了注意力机制作为独立建模工具的强大能力,开启了"注意力即一切"的新时代。

本研究的代码和预训练模型已在GitHub上开源(tensor2tensor),极大地推动了学术界和工业界对Transformer的研究与应用。截至目前,该论文已被引用超过95,000次,成为深度学习领域被引用最多的论文之一,其影响力持续扩展到计算机视觉(Vision Transformer)、语音识别、蛋白质结构预测(AlphaFold)等多个领域。`,
    keywords: ['Transformer', '自注意力机制', '机器翻译', '深度学习', '序列建模', 'NLP', 'Multi-Head Attention', '神经网络架构'],
    projectId: 'p-001',
    projectName: '面向大规模预训练模型的参数高效微调方法研究',
    projectCode: '62306124',
    source: 'manual_upload',
    sourceStage: 'design',
    projectPhase: 'development',
    sourceRef: 'internal-upload:r-007',
    syncTime: '2017-12-06 10:00',
    formatChecked: true,
    formatStatus: 'passed',
    formatNote: '论文格式规范,数据完整,实验充分',
    status: 'published',
    visibility: 'public_full',
    attachments: [
      { 
        id: 'att-7-1', 
        name: 'Attention_Is_All_You_Need_Full_Paper.pdf', 
        size: 1024 * 2048, 
        type: 'pdf', 
        url: '/mock/attention_paper.pdf', 
        uploadedAt: '2017-12-06',
        description: '论文完整版PDF,包含详细的模型架构图、实验结果和分析'
      },
      { 
        id: 'att-7-2', 
        name: 'Transformer_Architecture_Diagram.png', 
        size: 1024 * 512, 
        type: 'image', 
        url: '/mock/transformer_arch.png', 
        uploadedAt: '2017-12-06',
        description: 'Transformer完整架构示意图,展示编码器-解码器结构'
      },
      { 
        id: 'att-7-3', 
        name: 'Multi_Head_Attention_Illustration.png', 
        size: 1024 * 384, 
        type: 'image', 
        url: '/mock/multi_head_attention.png', 
        uploadedAt: '2017-12-06',
        description: '多头注意力机制的详细图示'
      },
      { 
        id: 'att-7-4', 
        name: 'WMT_Translation_Results.xlsx', 
        size: 1024 * 128, 
        type: 'excel', 
        url: '/mock/wmt_results.xlsx', 
        uploadedAt: '2017-12-06',
        description: 'WMT 2014英德、英法翻译任务的完整实验数据'
      },
      { 
        id: 'att-7-5', 
        name: 'Model_Hyperparameters.json', 
        size: 1024 * 8, 
        type: 'json', 
        url: '/mock/model_config.json', 
        uploadedAt: '2017-12-06',
        description: '模型超参数配置文件,包含层数、注意力头数、隐藏层维度等'
      },
      { 
        id: 'att-7-6', 
        name: 'Training_Curves.pdf', 
        size: 1024 * 256, 
        type: 'pdf', 
        url: '/mock/training_curves.pdf', 
        uploadedAt: '2017-12-06',
        description: '训练过程的损失曲线和BLEU分数变化趋势'
      },
      { 
        id: 'att-7-7', 
        name: 'Attention_Visualization_Examples.zip', 
        size: 1024 * 3072, 
        type: 'zip', 
        url: '/mock/attention_viz.zip', 
        uploadedAt: '2017-12-06',
        description: '注意力权重可视化示例,展示模型如何关注输入序列的不同部分'
      },
      { 
        id: 'att-7-8', 
        name: 'Supplementary_Materials.pdf', 
        size: 1024 * 512, 
        type: 'pdf', 
        url: '/mock/supplementary.pdf', 
        uploadedAt: '2017-12-06',
        description: '补充材料,包含额外的消融实验、模型变体对比和错误分析'
      }
    ],
    metadata: { 
      journal: 'NeurIPS 2017 (Conference on Neural Information Processing Systems)', 
      doi: '10.5555/3295222.3295349', 
      impact: 12.8, 
      publishDate: '2017-12-06',
      citations: 95000,
      conference: 'NeurIPS 2017',
      acceptanceRate: '20.9%',
      presentationType: 'Oral Presentation',
      awards: ['NeurIPS 2017 Best Paper Award', 'Most Influential Paper 2017-2022']
    },
    createdBy: '张教授',
    createdAt: '2017-06-12',
    updatedAt: '2017-12-10',
    reviewHistory: [
      { 
        id: 'rev-7-1', 
        reviewerId: '2', 
        reviewerName: '张教授', 
        action: 'approve', 
        comment: '这是一项开创性的工作,Transformer架构彻底改变了序列建模的范式。论文逻辑严密,实验充分,理论分析深入。多头注意力机制和位置编码的设计极具创新性。强烈建议发表。', 
        createdAt: '2017-09-15' 
      },
      { 
        id: 'rev-7-2', 
        reviewerId: '1', 
        reviewerName: '管理员', 
        action: 'approve', 
        comment: '实验结果令人印象深刻,在WMT翻译任务上超越了所有现有基准。论文撰写清晰,架构图示直观。这将成为里程碑式的工作。', 
        createdAt: '2017-09-20' 
      }
    ],
    impactMetrics: {
      citations: 95000,
      h_index_contribution: 156,
      altmetric_score: 4856,
      downloads: 1250000,
      github_stars: 45000,
      implementations: 'TensorFlow, PyTorch, JAX等主流框架均有官方实现',
      industryAdoption: 'Google Translate, DeepL, ChatGPT等产品核心技术'
    }
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

export const resultAccessRequests = [
  {
    id: 'req-001',
    resultId: 'r-005',
    resultTitle: '新能源固态电解质界面改性实验报告',
    resultType: 'paper',
    projectName: '高比能固态锂电池界面稳定性及调控机制',
    visibility: 'internal_abstract',
    userId: '3',
    userName: '李研究员',
    status: 'rejected',
    reason: '需要参考全文补充实验设计',
    createdAt: '2025-01-01 15:30',
    reviewedAt: '2025-01-02 10:00',
    reviewer: '张教授',
    comment: '请阐明数据保密措施后重新申请'
  },
  {
    id: 'req-002',
    resultId: 'r-002',
    resultTitle: '一种基于多传感器融合的车辆目标检测方法及系统',
    resultType: 'patent',
    projectName: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    visibility: 'internal_full',
    userId: '3',
    userName: '李研究员',
    status: 'pending',
    reason: '准备撰写企业转化方案，需要查看专利全文',
    createdAt: '2025-01-08 14:30'
  },
  {
    id: 'req-003',
    resultId: 'r-003',
    resultTitle: '分布式科研数据管理系统 V1.0',
    resultType: 'software',
    projectName: '数字经济驱动下制造业供应链韧性提升策略研究',
    visibility: 'internal_abstract',
    userId: '3',
    userName: '李研究员',
    status: 'pending',
    reason: '需要申请全文用于企业部署方案评审',
    createdAt: '2025-01-05 11:20'
  },
  {
    id: 'req-004',
    resultId: 'r-004',
    resultTitle: '多模态交通场景数据集 V2.0',
    resultType: 'paper',
    projectName: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    visibility: 'internal_full',
    userId: '3',
    userName: '李研究员',
    status: 'approved',
    reason: '拟开展跨部门联合研究，需要访问完整数据集',
    createdAt: '2024-12-26 09:40',
    reviewedAt: '2024-12-28 16:20',
    reviewer: '管理员',
    comment: '已确认保密承诺，准予查看全文及附件'
  }
]

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

// ==================== 中期成果物 Mock 数据 ====================
export const interimResults = [
  {
    id: 'ir-001',
    projectId: 'p-001',
    projectName: '面向大规模预训练模型的参数高效微调方法研究',
    projectCode: '62306124',
    projectPhase: '需求分析',
    name: '项目技术服务合同',
    type: 'contract',
    typeLabel: '合同',
    description: '与XX公司签订的技术服务合同',
    attachments: [
      {
        id: 'att-ir-001-1',
        name: '技术服务合同_20241220.pdf',
        url: '/mock/files/contract_001.pdf',
        size: 2458624,
        ext: 'pdf',
        uploadedAt: '2024-12-20 14:30:00'
      }
    ],
    submitter: '李研究员',
    submitterDept: '人工智能学院',
    submittedAt: '2024-12-20 14:30:00',
    syncedAt: '2024-12-20 15:00:00',
    source: 'process_system',
    sourceRef: 'PS-P001-001',
    sourceUrl: 'http://process.example.com/projects/p-001/deliverables/001',
    tags: ['重要', '已归档'],
    status: '已提交'
  },
  {
    id: 'ir-002',
    projectId: 'p-001',
    projectName: '面向大规模预训练模型的参数高效微调方法研究',
    projectCode: '62306124',
    projectPhase: '需求分析',
    name: '项目申报书',
    type: 'application',
    typeLabel: '申报书',
    description: '国家自然科学基金项目申报书',
    attachments: [
      {
        id: 'att-ir-002-1',
        name: '项目申报书_62306124.docx',
        url: '/mock/files/application_001.docx',
        size: 1856342,
        ext: 'docx',
        uploadedAt: '2024-12-15 10:20:00'
      },
      {
        id: 'att-ir-002-2',
        name: '附件1_研究基础.pdf',
        url: '/mock/files/application_001_att1.pdf',
        size: 845632,
        ext: 'pdf',
        uploadedAt: '2024-12-15 10:20:00'
      }
    ],
    submitter: '张教授',
    submitterDept: '人工智能学院',
    submittedAt: '2024-12-15 10:20:00',
    syncedAt: '2024-12-15 11:00:00',
    source: 'process_system',
    sourceRef: 'PS-P001-002',
    sourceUrl: 'http://process.example.com/projects/p-001/deliverables/002',
    tags: ['重要'],
    status: '已提交'
  },
  {
    id: 'ir-003',
    projectId: 'p-002',
    projectName: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    projectCode: '2024YFB3301200',
    projectPhase: '可行性研究',
    name: '项目可行性研究报告',
    type: 'feasibility_report',
    typeLabel: '可行性报告',
    description: '技术可行性和经济可行性分析报告',
    attachments: [
      {
        id: 'att-ir-003-1',
        name: '可行性研究报告.pdf',
        url: '/mock/files/feasibility_001.pdf',
        size: 5234567,
        ext: 'pdf',
        uploadedAt: '2024-11-10 16:45:00'
      }
    ],
    submitter: '王博士',
    submitterDept: '电子信息学院',
    submittedAt: '2024-11-10 16:45:00',
    syncedAt: '2024-11-10 17:00:00',
    source: 'process_system',
    sourceRef: 'PS-P002-001',
    sourceUrl: 'http://process.example.com/projects/p-002/deliverables/001',
    tags: ['已归档'],
    status: '已提交'
  },
  {
    id: 'ir-004',
    projectId: 'p-002',
    projectName: '基于多模态融合的复杂场景自动驾驶感知关键技术',
    projectCode: '2024YFB3301200',
    projectPhase: '需求分析',
    name: '系统需求规格说明书',
    type: 'requirement_doc',
    typeLabel: '需求文档',
    description: '详细的系统功能和性能需求说明',
    attachments: [
      {
        id: 'att-ir-004-1',
        name: '需求规格说明书V1.0.docx',
        url: '/mock/files/requirement_001.docx',
        size: 3145728,
        ext: 'docx',
        uploadedAt: '2024-12-05 09:30:00'
      }
    ],
    submitter: '李研究员',
    submitterDept: '电子信息学院',
    submittedAt: '2024-12-05 09:30:00',
    syncedAt: '2024-12-05 10:00:00',
    source: 'process_system',
    sourceRef: 'PS-P002-002',
    sourceUrl: 'http://process.example.com/projects/p-002/deliverables/002',
    tags: [],
    status: '已提交'
  },
  {
    id: 'ir-005',
    projectId: 'p-003',
    projectName: '高比能固态锂电池界面稳定性及调控机制',
    projectCode: '52372056',
    projectPhase: '设计阶段',
    name: '技术方案设计文档',
    type: 'design_doc',
    typeLabel: '设计文档',
    description: '界面改性技术方案详细设计',
    attachments: [
      {
        id: 'att-ir-005-1',
        name: '技术方案设计V2.0.pdf',
        url: '/mock/files/design_001.pdf',
        size: 4567890,
        ext: 'pdf',
        uploadedAt: '2024-10-20 14:00:00'
      },
      {
        id: 'att-ir-005-2',
        name: '设计图纸.dwg',
        url: '/mock/files/design_001_drawing.dwg',
        size: 2345678,
        ext: 'dwg',
        uploadedAt: '2024-10-20 14:00:00'
      }
    ],
    submitter: '陈教授',
    submitterDept: '材料科学学院',
    submittedAt: '2024-10-20 14:00:00',
    syncedAt: '2024-10-20 15:00:00',
    source: 'process_system',
    sourceRef: 'PS-P003-001',
    sourceUrl: 'http://process.example.com/projects/p-003/deliverables/001',
    tags: ['重要'],
    status: '已提交'
  },
  {
    id: 'ir-006',
    projectId: 'p-004',
    projectName: '气候变化背景下典型湿地生态系统碳汇功能演变特征',
    projectCode: '32370412',
    projectPhase: '执行中',
    name: '2024年第一季度进展报告',
    type: 'progress_report',
    typeLabel: '进展报告',
    description: '项目执行情况和阶段性成果总结',
    attachments: [
      {
        id: 'att-ir-006-1',
        name: 'Q1进展报告.pdf',
        url: '/mock/files/progress_001.pdf',
        size: 1234567,
        ext: 'pdf',
        uploadedAt: '2024-03-31 17:00:00'
      }
    ],
    submitter: '刘博士',
    submitterDept: '生态环境学院',
    submittedAt: '2024-03-31 17:00:00',
    syncedAt: '2024-04-01 09:00:00',
    source: 'process_system',
    sourceRef: 'PS-P004-001',
    sourceUrl: 'http://process.example.com/projects/p-004/deliverables/001',
    tags: [],
    status: '已提交'
  },
  {
    id: 'ir-007',
    projectId: 'p-005',
    projectName: '针对耐药菌的新型抗菌肽设计及其膜穿孔作用机理研究',
    projectCode: '82303451',
    projectPhase: '测试阶段',
    name: '抗菌活性测试报告',
    type: 'test_report',
    typeLabel: '测试报告',
    description: '新型抗菌肽的体外抗菌活性测试结果',
    attachments: [
      {
        id: 'att-ir-007-1',
        name: '抗菌活性测试报告.pdf',
        url: '/mock/files/test_001.pdf',
        size: 3456789,
        ext: 'pdf',
        uploadedAt: '2024-09-15 11:30:00'
      },
      {
        id: 'att-ir-007-2',
        name: '实验数据.xlsx',
        url: '/mock/files/test_001_data.xlsx',
        size: 567890,
        ext: 'xlsx',
        uploadedAt: '2024-09-15 11:30:00'
      }
    ],
    submitter: '赵副教授',
    submitterDept: '生物医药学院',
    submittedAt: '2024-09-15 11:30:00',
    syncedAt: '2024-09-15 12:00:00',
    source: 'process_system',
    sourceRef: 'PS-P005-001',
    sourceUrl: 'http://process.example.com/projects/p-005/deliverables/001',
    tags: [],
    status: '已提交'
  },
  {
    id: 'ir-008',
    projectId: 'p-006',
    projectName: '数字经济驱动下制造业供应链韧性提升策略研究',
    projectCode: '72371089',
    projectPhase: '需求分析',
    name: '研究合作协议',
    type: 'contract',
    typeLabel: '合同',
    description: '与企业签订的研究合作协议',
    attachments: [
      {
        id: 'att-ir-008-1',
        name: '研究合作协议.pdf',
        url: '/mock/files/contract_002.pdf',
        size: 1987654,
        ext: 'pdf',
        uploadedAt: '2023-12-10 15:20:00'
      }
    ],
    submitter: '孙教授',
    submitterDept: '管理学院',
    submittedAt: '2023-12-10 15:20:00',
    syncedAt: '2023-12-10 16:00:00',
    source: 'process_system',
    sourceRef: 'PS-P006-001',
    sourceUrl: 'http://process.example.com/projects/p-006/deliverables/001',
    tags: ['已归档'],
    status: '已提交'
  }
]
