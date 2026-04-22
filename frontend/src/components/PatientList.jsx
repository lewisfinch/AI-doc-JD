import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'

const DEFAULT_USER_ID = 1

function PatientList() {
  const navigate = useNavigate()
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deletingId, setDeletingId] = useState(null)
  const [selectedPatientId, setSelectedPatientId] = useState(() => {
    const cached = localStorage.getItem('selectedPatientId')
    return cached ? Number(cached) : null
  })

  const loadPatients = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await api.get('/patient', {
        params: { userId: DEFAULT_USER_ID },
      })
      if (response.data?.code !== 0) {
        setError(response.data?.message || '就诊人列表加载失败')
        return
      }
      setPatients(response.data?.data || [])
    } catch (e) {
      setError('就诊人列表加载失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPatients()
  }, [])

  const handleSelect = (patientId) => {
    localStorage.setItem('selectedPatientId', String(patientId))
    setSelectedPatientId(patientId)
    navigate(`/chat?patientId=${patientId}`)
  }

  const handleDelete = async (patientId) => {
    const confirmed = window.confirm('确定删除该就诊人吗？')
    if (!confirmed) return

    setDeletingId(patientId)
    try {
      const response = await api.delete(`/patient/${patientId}`, {
        params: { userId: DEFAULT_USER_ID },
      })
      if (response.data?.code !== 0) {
        window.alert(response.data?.message || '删除失败')
        return
      }

      if (selectedPatientId === patientId) {
        localStorage.removeItem('selectedPatientId')
        setSelectedPatientId(null)
      }
      setPatients((prev) => prev.filter((item) => item.id !== patientId))
    } catch (e) {
      window.alert('删除失败，请稍后重试')
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="p-4 pb-24">
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-800">我的就诊人</h1>
        <button
          onClick={() => navigate('/patients/add')}
          className="rounded-lg bg-blue-600 px-3 py-2 text-sm text-white"
        >
          新增就诊人
        </button>
      </div>

      {loading && <div className="text-sm text-gray-500">加载中...</div>}

      {!loading && error && (
        <div className="rounded-lg bg-red-50 p-3 text-sm text-red-500">{error}</div>
      )}

      {!loading && !error && patients.length === 0 && (
        <div className="rounded-lg bg-white p-4 text-sm text-gray-500 shadow">
          暂无就诊人，请先新增。
        </div>
      )}

      <div className="space-y-3">
        {patients.map((item) => (
          <div
            key={item.id}
            className={`rounded-xl bg-white p-4 shadow ${
              selectedPatientId === item.id ? 'ring-2 ring-blue-500' : ''
            }`}
          >
            <div className="mb-2 flex items-center justify-between">
              <div className="text-base font-medium text-gray-800">{item.name}</div>
              <div className="text-xs text-gray-500">{item.relation}</div>
            </div>
            <div className="mb-1 text-sm text-gray-600">
              {item.gender} · {item.age}岁
            </div>
            <div className="mb-3 text-sm text-gray-500">
              既往史：{item.medicalHistory || '无'}
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => handleSelect(item.id)}
                className="flex-1 rounded-lg bg-blue-600 px-3 py-2 text-sm text-white"
              >
                设为当前就诊人并问诊
              </button>
              <button
                onClick={() => handleDelete(item.id)}
                disabled={deletingId === item.id}
                className="rounded-lg border border-red-200 px-3 py-2 text-sm text-red-500 disabled:opacity-50"
              >
                {deletingId === item.id ? '删除中...' : '删除'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default PatientList