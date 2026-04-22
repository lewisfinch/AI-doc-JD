import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'

function Home() {
  const navigate = useNavigate()

  const greeting = useMemo(() => {
    const hour = new Date().getHours()
    if (hour < 11) return '早上好'
    if (hour < 18) return '下午好'
    return '晚上好'
  }, [])

  const entrances = [
    {
      title: '极速问诊',
      desc: '智能医生在线问答，快速获得初步建议',
      icon: '💬',
      bg: 'from-blue-500 to-indigo-500',
      onClick: () => navigate('/chat'),
    },
    {
      title: '报告解读',
      desc: '上传化验/体检/影像报告，AI 分析重点异常',
      icon: '📄',
      bg: 'from-violet-500 to-fuchsia-500',
      onClick: () => navigate('/report'),
    },
    {
      title: '用药助手',
      desc: '查询药品适应症、禁忌与不良反应',
      icon: '💊',
      bg: 'from-emerald-500 to-teal-500',
      onClick: () => navigate('/medication'),
    },
  ]

  const articles = [
    {
      id: 1,
      title: '体检报告看不懂？先看这 4 个关键指标',
      summary: '血压、血糖、血脂、尿酸是慢病管理的核心入口，读懂它们能更早发现健康风险。',
      image:
        'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=1000&q=80',
    },
    {
      id: 2,
      title: '家庭常备药清单：成年人建议配置',
      summary: '退热镇痛、肠胃用药、外伤处理药品建议分层配置，并注意有效期与禁忌人群。',
      image:
        'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1000&q=80',
    },
    {
      id: 3,
      title: '睡眠与免疫：为什么总熬夜更容易生病',
      summary: '规律作息可改善炎症反应与代谢水平，建议固定入睡时间并减少晚间高强度刺激。',
      image:
        'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1000&q=80',
    },
  ]

  return (
    <div className="mx-auto max-w-6xl p-4 pb-24 md:p-6">
      <section className="rounded-2xl bg-gradient-to-r from-sky-600 to-blue-700 p-6 text-white shadow-lg">
        <div className="text-sm text-blue-100">{greeting}</div>
        <h1 className="mt-1 text-2xl font-bold md:text-3xl">欢迎来到 AI 医疗助手</h1>
        <p className="mt-2 text-sm text-blue-100 md:text-base">让问诊、报告解读和用药咨询更高效、更安心。</p>
        <button
          onClick={() => navigate('/chat')}
          className="mt-4 flex w-full items-center rounded-xl bg-white/20 px-4 py-3 text-left text-sm text-blue-50 backdrop-blur transition hover:bg-white/30"
        >
          🔍 你可以试试：最近总是头痛，应该挂什么科？
        </button>
      </section>

      <section className="mt-6">
        <div className="mb-3 text-lg font-semibold text-gray-800">核心功能</div>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {entrances.map((item) => (
            <button
              key={item.title}
              onClick={item.onClick}
              className={`rounded-2xl bg-gradient-to-r ${item.bg} p-5 text-left text-white shadow-md transition hover:scale-[1.02]`}
            >
              <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-full bg-white/20 text-xl">
                {item.icon}
              </div>
              <div className="text-lg font-semibold">{item.title}</div>
              <div className="mt-1 text-sm text-white/90">{item.desc}</div>
            </button>
          ))}
        </div>
      </section>

      <section className="mt-8">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-800">健康科普</h2>
          <span className="text-xs text-gray-400">精选内容</span>
        </div>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {articles.map((article) => (
            <button
              key={article.id}
              onClick={() => navigate(`/article/${article.id}`)}
              className="overflow-hidden rounded-2xl bg-white text-left shadow-sm ring-1 ring-gray-100 transition hover:shadow-md"
            >
              <img src={article.image} alt={article.title} className="h-40 w-full object-cover" />
              <div className="p-4">
                <h3 className="line-clamp-2 text-base font-semibold text-gray-800">{article.title}</h3>
                <p className="mt-2 line-clamp-3 text-sm leading-6 text-gray-600">{article.summary}</p>
                <div className="mt-3 text-xs font-medium text-blue-600">查看详情 →</div>
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}

export default Home