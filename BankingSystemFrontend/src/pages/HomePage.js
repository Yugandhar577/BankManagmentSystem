// --- src/pages/HomePage.js ---
import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const HomePage = () => {
  const { user } = useAuth();

  return (
    <div className="bg-white p-8 rounded-lg shadow-xl text-center">
      <h1 className="text-4xl font-bold text-blue-800 mb-4">Welcome to BankSys</h1>
      <p className="text-lg text-gray-700 mb-8">
        Your reliable and secure partner in digital banking.
      </p>
      <div className="space-x-4">
        {!user ? (
          <>
            <Link
              to="/login"
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg text-lg transition duration-300"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-6 rounded-lg text-lg transition duration-300"
            >
              Register
            </Link>
          </>
        ) : (
          <Link
            to={user.role === 'Admin' ? "/admin" : "/dashboard"}
            className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg text-lg transition duration-300"
          >
            Go to Your Dashboard
          </Link>
        )}
      </div>
    </div>
  );
};

export default HomePage;