// --- src/components/admin/AdminDashboard.js ---
import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const AdminDashboard = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-8">
      <h1 className="text-3xl font-bold text-gray-800">Admin Dashboard</h1>
      <p className="text-lg text-gray-600">
        Welcome, Admin {user?.firstName}.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Link to="/admin/users" className="block p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow">
          <h2 className="text-2xl font-semibold text-blue-700 mb-2">User Management</h2>
          <p className="text-gray-600">
            View, create, edit, and delete user accounts.
          </p>
        </Link>

        <Link to="/admin/transactions" className="block p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow">
          <h2 className="text-2xl font-semibold text-green-700 mb-2">All Transactions</h2>
          <p className="text-gray-600">
            View a complete log of all transactions in the system.
          </p>
        </Link>
      </div>
    </div>
  );
};

export default AdminDashboard;