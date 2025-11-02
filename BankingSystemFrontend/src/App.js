import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

// Common Components
import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import NotFoundPage from './pages/NotFoundPage';

// Customer Components
import Dashboard from './components/customer/Dashboard';
import Deposit from './components/customer/Deposit';
import Withdraw from './components.customer/Withdraw';
import Transfer from './components/customer/Transfer';
import TransactionHistory from './components/customer/TransactionHistory';

// Admin Components
import AdminDashboard from './components/admin/AdminDashboard';
import UserManagement from './components/admin/UserManagement';
import AllTransactions from './components/admin/AllTransactions';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <main className="container">
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Customer Routes (Protected) */}
            <Route path="/dashboard" element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/deposit" element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <Deposit />
              </ProtectedRoute>
            } />
            <Route path="/withdraw" element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <Withdraw />
              </ProtectedRoute>
            } />
            <Route path="/transfer" element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <Transfer />
              </ProtectedRoute>
            } />
            <Route path="/history" element={
              <ProtectedRoute roles={['CUSTOMER']}>
                <TransactionHistory />
              </ProtectedRoute>
            } />

            {/* Admin Routes (Protected) */}
            <Route path="/admin" element={
              <ProtectedRoute roles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            } />
            <Route path="/admin/users" element={
              <ProtectedRoute roles={['ADMIN']}>
                <UserManagement />
              </ProtectedRoute>
            } />
            <Route path="/admin/transactions" element={
              <ProtectedRoute roles={['ADMIN']}>
                <AllTransactions />
              </ProtectedRoute>
            } />

            {/* Catch-all and Redirects */}
            <Route path="/" element={<Navigate to="/login" />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </Router>
    </AuthProvider>
  );
}

export default App;