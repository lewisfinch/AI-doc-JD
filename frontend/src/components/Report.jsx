import { useMemo, useRef, useState } from 'react'
import api from '../api'

const DEFAULT_USER_ID = 1
const DEFAULT_REPORT_TYPE = 2

function Report() {
  const fileInputRef = useRef(null)
  const [dragging, setDragging] = useState(false)
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const analysisText = result?.analysisResult || ''

  const sections = useMemo(() => {
    if (!analysisText) return []
    return analysisText
      .replace(/\r/g, '')
      .split(/(?=^\s*[1-3][).、]\s*)/m)
      .map((item) => item.trim())
      .filter(Boolean)
  }, [analysisText])

  const setSelectedFile = (nextFile) => {
    if (!nextFile) return
    setFile(nextFile)
    setError('')
  }

  const handleDrop = (e) => {
    e.preventDefault()
    setDragging(false)
    const dropped = e.dataTransfer.files?.[0]
    setSelectedFile(dropped)
  }

  const handleUpload = async () => {
    if (!file) {
      setError('请先选择报告文件')
      return
    }

    const patientId = Number(localStorage.getItem('selectedPatientId')) || 1

    setLoading(true)
    setError('')
    setResult(null)

    try {
      const formData = new FormData()
      formData.append('userId', String(DEFAULT_USER_ID))
      formData.append('patientId', String(patientId))
      formData.append('reportType', String(DEFAULT_REPORT_TYPE))
      formData.append('file', file)

      const response = await api.post('/report/analyze', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })

      if (response.data?.code !== 0) {
        setError(response.data?.message || '报告解读失败')
        return
      }

      setResult(response.data?.data || null)
    } catch (e) {
      setError('报告解读失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-5xl p-4 pb-24 md:p-6">
      <div className="mb-6 rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-white shadow-lg">
        <h1 className="text-2xl font-bold md:text-3xl">报告解读</h1>
        <p className="mt-2 text-sm text-blue-100 md:text-base">
          上传体检/化验/影像报告，AI 将输出异常指标、风险提示与生活干预建议。
        </p>
      </div>

      <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100 md:p-6">
        <div
          onClick={() => fileInputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault()
            setDragging(true)
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={handleDrop}
          className={`cursor-pointer rounded-2xl border-2 border-dashed p-8 text-center transition ${
            dragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-400 hover:bg-gray-50'
          }`}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.txt,.md,.text,.png,.jpg,.jpeg,.bmp"
            className="hidden"
            onChange={(e) => setSelectedFile(e.target.files?.[0])}
          />
          <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-blue-100 text-2xl">📄</div>
          <div className="text-sm font-medium text-gray-800 md:text-base">拖拽报告到此处，或点击上传</div>
          <div className="mt-1 text-xs text-gray-500">支持 PDF / TXT / 图片（png, jpg, jpeg, bmp）</div>
        {file && (
            <div className="mt-3 inline-flex items-center rounded-full bg-green-50 px-3 py-1 text-xs text-green-700">
              已选择：{file.name}
            </div>
          )}
        </div>

        <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center">
          <button
            disabled={loading}
            onClick={handleUpload}
            className="inline-flex items-center justify-center rounded-xl bg-blue-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
          >
            {loading ? (
              <span className="inline-flex items-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-r-transparent" />
                AI 正在解读...
              </span>
            ) : (
              '开始解读'
            )}
          </button>
          <div className="text-xs text-gray-500">
            请求方式：POST /api/report/analyze，Content-Type: multipart/form-data
          </div>
        </div>

        {error && <div className="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-600">{error}</div>}
      </div>

      {loading && (
        <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
          {[1, 2, 3].map((item) => (
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
        <div className="mt-6 space-y-4">
          <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100 md:p-6">
            <div className="mb-2 text-sm text-gray-500">
              解读文件：{result.fileName || '-'}（报告ID：{result.reportId || '-'}）
            </div>
            <h2 className="text-lg font-semibold text-gray-800">AI 解读结果</h2>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            {['异常指标', '可能风险', '生活干预建议'].map((title, index) => (
              <div key={title} className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100 md:p-5">
                <h3 className="mb-3 text-base font-semibold text-gray-800">{title}</h3>
                <pre className="whitespace-pre-wrap break-words text-sm leading-6 text-gray-700">
                  {sections[index] || '该部分暂无结构化内容，请查看完整原文。'}
                </pre>
              </div>
            ))}
          </div>

          <details className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-gray-100">
            <summary className="cursor-pointer text-sm font-medium text-gray-700">查看完整解读原文</summary>
            <pre className="mt-3 whitespace-pre-wrap break-words text-sm leading-6 text-gray-700">{analysisText}</pre>
          </details>
        </div>
      )}
    </div>
  )
}

export default Report