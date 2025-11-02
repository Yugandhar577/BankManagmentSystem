// --- src/components/common/Navbar.js ---
import React from 'react';
import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();

  const activeLinkClass = "text-white bg-blue-700";
  const inactiveLinkClass = "text-gray-300 hover:bg-blue-600 hover:text-white";
  const navLinkClasses = ({ isActive }) =>
    `px-3 py-2 rounded-md text-sm font-medium ${isActive ? activeLinkClass : inactiveLinkClass}`;

  return (
    <nav className="bg-blue-800 shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="text-white text-2xl font-bold">
            BankSys
          </Link>
          <div className="flex items-center space-x-4">
            {!user ? (
              <>
                <NavLink to="/login" className={navLinkClasses}>
                  Login
                </NavLink>
                <NavLink to="/register" className="bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-md text-sm font-medium">
                  Register
                </NavLink>
              </>
            ) : (
              <>
                <span className="text-gray-300">Welcome, {user.firstName}!</span>
                
                {user.role === 'Admin' ? (
                  <NavLink to="/admin" className={navLinkClasses}>
                    Admin Panel
                  </NavLink>
                ) : (
                  <NavLink to="/dashboard" className={navLinkClasses}>
                    Dashboard
                  </NavLink>
                )}

                <NavLink to="/history" className={navLinkClasses}>
                  History
                </NavLink>

                <button
                  onClick={logout}
                  className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-md text-sm font-medium"
                >
                  Logout
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;