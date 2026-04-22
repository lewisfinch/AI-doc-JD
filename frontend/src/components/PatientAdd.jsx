import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'

const DEFAULT_USER_ID = 1

function PatientAdd() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    name: '',
    age: '',
    relation: '',
    medicalHistory: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const updateField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!form.name.trim()) {
      setError('请输入姓名')
      return
    }
    if (!form.relation.trim()) {
      setError('请输入关系')
      return
    }
    const age = Number(form.age)
    if (!age || age <= 0 || age > 150) {
      setError('请输入合法年龄')
      return
    }

    setSubmitting(true)
    try {
      const response = await api.post('/patient', {
        userId: DEFAULT_USER_ID,
        name: form.name.trim(),
        gender: form.gender,
        age,
        relation: form.relation.trim(),
        medicalHistory: form.medicalHistory.trim(),
      })

      if (response.data?.code !== 0) {
        setError(response.data?.message || '新增失败')
        return
      }

      window.alert('新增成功')
      navigate('/patients')
    } catch (err) {
      setError('新增失败，请稍后重试')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="p-4 pb-24">
      <h1 className="mb-4 text-xl font-semibold text-gray-800">新增就诊人</h1>

      <form onSubmit={handleSubmit} className="space-y-3 rounded-xl bg-white p-4 shadow">
        <div>
          <label className="mb-1 block text-sm text-gray-600">姓名</label>
          <input
            value={form.name}
            onChange={(e) => updateField('name', e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue-500"
            placeholder="请输入姓名"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-600">性别</label>
          <select
            value={form.gender}
            onChange={(e) => updateField('gender', e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue-500"
          >
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-600">年龄</label>
          <input
            type="number"
            min="1"
            max="150"
            value={form.age}
            onChange={(e) => updateField('age', e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue-500"
            placeholder="请输入年龄"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-600">关系</label>
          <input
            value={form.relation}
            onChange={(e) => updateField('relation', e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue-500"
            placeholder="例如：本人/父亲/母亲/孩子"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-600">既往史</label>
          <textarea
            rows="3"
            value={form.medicalHistory}
            onChange={(e) => updateField('medicalHistory', e.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue-500"
            placeholder="例如：高血压3年，长期服药"
          />
        </div>

        {error && <div className="rounded-lg bg-red-50 p-2 text-sm text-red-500">{error}</div>}

        <div className="flex gap-2 pt-1">
          <button
            type="button"
            onClick={() => navigate('/patients')}
            className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-600"
          >
            取消
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="flex-1 rounded-lg bg-blue-600 px-3 py-2 text-sm text-white disabled:opacity-50"
          >
            {submitting ? '提交中...' : '保存'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default PatientAdd