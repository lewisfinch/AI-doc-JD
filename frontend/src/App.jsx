import React from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Chat from './Chat'

function App() {
  return (
      <Router>
        <Routes>
          {/* 这里的意思是：当访问根路径 / 时，展示我们自己写的 Chat 组件 */}
          <Route path="/" element={<Chat />} />
        </Routes>
      </Router>
  )
}

export default App