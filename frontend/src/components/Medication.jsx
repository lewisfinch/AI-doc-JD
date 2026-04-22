import { useMemo, useState } from 'react'
import api from '../api'

function Medication() {
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState('')

  const sections = useMemo(() => {
    if (!result) return []
    return result
      .replace(/\r/g, '')
      .split(/(?=^\s*[1-4][).、]\s*)/m)
      .map((item) => item.trim())
      .filter(Boolean)
  }, [result])

  const handleSearch = async () => {
    if (!query.trim()) {
      setError('请输入药品名称')
      return
    }

    setLoading(true)
    setError('')
    setResult('')

    try {
      const response = await api.post(
        '/medication/consult',
        { message: query.trim() },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      )

      if (response.data?.code !== 0) {
        setError(response.data?.message || '用药咨询失败')
        return
      }

      setResult(response.data?.data || '')
    } catch (e) {
      setError('用药咨询失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-5xl p-4 pb-24 md:p-6">
      <div className="mb-6 rounded-2xl bg-gradient-to-r from-emerald-600 to-teal-600 p-6 text-white shadow-lg">
        <h1 className="text-2xl font-bold md:text-3xl">用药助手</h1>
        <p className="mt-2 text-sm text-emerald-50 md:text-base">
          输入药品名称，快速获得适应症、用法用量、禁忌与不良反应建议。
        </p>
      </div>

      <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100 md:p-6">
        <div className="flex flex-col gap-3 sm:flex-row">
          <div className="relative flex-1">
            <span className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-gray-400">🔎</span>
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleSearch()
              }}
              placeholder="请输入药品名称，如：布洛芬"
              className="w-full rounded-xl border border-gray-200 py-3 pl-10 pr-4 text-sm outline-none transition focus:border-emerald-500"
            />
          </div>
          <button
            disabled={loading}
            onClick={handleSearch}
            className="rounded-xl bg-emerald-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-emerald-300"
          >
            {loading ? '查询中...' : '搜索'}
          </button>
        </div>
        <div className="mt-2 text-xs text-gray-500">
          请求方式：POST /api/medication/consult，Content-Type: application/json
        </div>
        {error && <div className="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-600">{error}</div>}
      </div>

      {loading && (
        <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
          {[1, 2, 3, 4].map((item) => (
            <div key={item} className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100">
              <div className="mb-3 h-4 w-24 animate-pulse rounded bg-gray-200" />
              <div className="h-3 w-full animate-pulse rounded bg-gray-100" />
              <div className="mt-2 h-3 w-5/6 animate-pulse rounded bg-gray-100" />
              <div className="mt-2 h-3 w-2/3 animate-pulse rounded bg-gray-100" />
            </div>
          ))}
        </div>
      )}

      {!loading && result && (
        <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
          {[
            { title: '适应症', color: 'text-blue-600' },
            { title: '用法用量', color: 'text-emerald-600' },
            { title: '禁忌症与注意事项', color: 'text-rose-600' },
            { title: '不良反应', color: 'text-amber-600' },
          ].map((card, index) => (
            <div key={card.title} className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100 md:p-5">
              <h3 className={`mb-3 text-base font-semibold ${card.color}`}>{card.title}</h3>
              <pre className="whitespace-pre-wrap break-words text-sm leading-6 text-gray-700">
                {sections[index] || '该部分暂无结构化内容，请查看完整原文。'}
              </pre>
            </div>
          ))}

          <details className="md:col-span-2 rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100">
            <summary className="cursor-pointer text-sm font-medium text-gray-700">查看完整咨询结果</summary>
            <pre className="mt-3 whitespace-pre-wrap break-words text-sm leading-6 text-gray-700">{result}</pre>
          </details>
        </div>
      )}
    </div>
  )
}

export default Medication