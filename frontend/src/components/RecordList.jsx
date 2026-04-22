import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'

const DEFAULT_USER_ID = 1

function RecordList() {
  const navigate = useNavigate()
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const selectedPatientId = localStorage.getItem('selectedPatientId')

  const loadRecords = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await api.get('/consultation', {
        params: {
          userId: DEFAULT_USER_ID,
          patientId: selectedPatientId ? Number(selectedPatientId) : undefined,
        },
      })
      if (response.data?.code !== 0) {
        setError(response.data?.message || '问诊记录加载失败')
        return
      }
      setRecords(response.data?.data || [])
    } catch (e) {
      setError('问诊记录加载失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRecords()
  }, [])

  const openSession = (session) => {
    navigate(`/chat?memoryId=${session.memoryId}&patientId=${session.patientId || ''}`)
  }

  return (
    <div className="p-4 pb-24">
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-800">问诊记录</h1>
        <button
          onClick={() => navigate('/chat')}
          className="rounded-lg bg-blue-600 px-3 py-2 text-sm text-white"
        >
          新建问诊
        </button>
      </div>

      {loading && <div className="text-sm text-gray-500">加载中...</div>}
      {!loading && error && (
        <div className="rounded-lg bg-red-50 p-3 text-sm text-red-500">{error}</div>
      )}
      {!loading && !error && records.length === 0 && (
        <div className="rounded-lg bg-white p-4 text-sm text-gray-500 shadow">
          暂无问诊记录，快去发起一次问诊吧。
        </div>
      )}

      <div className="space-y-3">
        {records.map((item) => (
          <div key={item.id} className="rounded-xl bg-white p-4 shadow">
            <div className="mb-1 text-base font-medium text-gray-800">{item.title || '新建问诊'}</div>
            <div className="mb-3 text-xs text-gray-500">memoryId: {item.memoryId}</div>
            <div className="mb-3 text-xs text-gray-500">
              patientId: {item.patientId || '-'} · 创建时间: {item.createTime || '-'}
            </div>
            <button
              onClick={() => openSession(item)}
              className="rounded-lg bg-blue-600 px-3 py-2 text-sm text-white"
            >
              继续会话
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

export default RecordList