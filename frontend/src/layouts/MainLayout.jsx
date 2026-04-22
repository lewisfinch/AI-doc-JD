import { NavLink, Outlet } from 'react-router-dom'

const tabs = [
  { to: '/', label: '首页' },
  { to: '/records', label: '问诊记录' },
  { to: '/patients', label: '我的' },
]

function MainLayout() {
  return (
    <div className="min-h-screen bg-gray-50 pb-16">
      <main className="min-h-[calc(100vh-4rem)]">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 left-0 right-0 h-16 border-t border-gray-200 bg-white">
        <div className="mx-auto flex h-full max-w-md">
          {tabs.map((tab) => (
            <NavLink
              key={tab.to}
              to={tab.to}
              end={tab.to === '/'}
              className={({ isActive }) =>
                `flex flex-1 items-center justify-center text-sm font-medium ${
                  isActive ? 'text-blue-600' : 'text-gray-500'
                }`
              }
            >
              {tab.label}
            </NavLink>
          ))}
        </div>
      </nav>
    </div>
  )
}

export default MainLayout