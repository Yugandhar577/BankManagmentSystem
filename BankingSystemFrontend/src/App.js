// --- src/App.js ---
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';
import ProtectedRoute from './components/common/ProtectedRoute';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import NotFoundPage from './pages/NotFoundPage';
import HomePage from './pages/HomePage';

// Customer Components
import Dashboard from './components/customer/Dashboard';
import Deposit from './components/customer/Deposit';
import Withdraw from './components/customer/Withdraw';
import Transfer from './components/customer/Transfer';
import TransactionHistory from './components/customer/TransactionHistory';

// Admin Components
import AdminDashboard from './components/admin/AdminDashboard';
import UserManagement from './components/admin/UserManagement';
import AllTransactions from './components/admin/AllTransactions';

function App() {
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />
      <main className="flex-grow container mx-auto p-4 md:p-8">
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Customer Routes */}
          <Route
            path="/dashboard"
            element={<ProtectedRoute><Dashboard /></ProtectedRoute>}
          />
          <Route
            path="/deposit"
            element={<ProtectedRoute><Deposit /></ProtectedRoute>}
          />
          <Route
            path="/withdraw"
            element={<ProtectedRoute><Withdraw /></ProtectedRoute>}
          />
          <Route
            path="/transfer"
            element={<ProtectedRoute><Transfer /></ProtectedRoute>}
          />
          <Route
            path="/history"
            element={<ProtectedRoute><TransactionHistory /></ProtectedRoute>}
          />

          {/* Admin Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute adminOnly={true}><AdminDashboard /></ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute adminOnly={true}><UserManagement /></ProtectedRoute>
            }
          />
          <Route
            path="/admin/transactions"
            element={
              <ProtectedRoute adminOnly={true}><AllTransactions /></ProtectedRoute>
            }
          />
          
          {/* 404 Not Found */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default App;