// --- src/components/common/ProtectedRoute.js ---
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

// Simple spinner component
const Spinner = () => (
  <div className="border-4 border-gray-200 border-t-blue-500 rounded-full w-12 h-12 animate-spin" role="status">
    <span className="sr-only">Loading...</span>
  </div>
);

const ProtectedRoute = ({ children, adminOnly = false }) => {
  const { user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Spinner />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && user.role !== 'Admin') {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default ProtectedRoute;