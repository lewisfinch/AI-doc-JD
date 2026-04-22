import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Chat from './Chat'
import Home from './components/Home'
import RecordList from './components/RecordList'
import RecordDetail from './components/RecordDetail'
import PatientList from './components/PatientList'
import PatientAdd from './components/PatientAdd'
import Report from './components/Report'
import Medication from './components/Medication'
import ArticleDetail from './components/ArticleDetail'
import MainLayout from './layouts/MainLayout'

function App() {
  return (
    <Router>
      <Routes>
        <Route element={<MainLayout />}>
          <Route index element={<Home />} />
          <Route path="/records" element={<RecordList key="records" />} />
          <Route path="/patients" element={<PatientList key="patients" />} />
        </Route>

        <Route path="/chat" element={<Chat />} />
        <Route path="/report" element={<Report />} />
        <Route path="/medication" element={<Medication />} />
        <Route path="/records/:id" element={<RecordDetail />} />
        <Route path="/patients/add" element={<PatientAdd />} />
        <Route path="/article/:id" element={<ArticleDetail />} />
      </Routes>
    </Router>
  )
}

export default App