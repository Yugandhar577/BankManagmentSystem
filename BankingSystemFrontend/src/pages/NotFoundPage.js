// --- src/pages/NotFoundPage.js ---
import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => {
  return (
    <div className="text-center">
      <h1 className="text-6xl font-bold text-blue-800">404</h1>
      <p className="text-2xl mt-4 mb-8">Page Not Found</p>
      <p className="text-gray-600 mb-8">
        Sorry, the page you are looking for does not exist.
      </p>
      <Link
        to="/"
        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg text-lg transition duration-300"
      >
        Go Back Home
      </Link>
    </div>
  );
};

export default NotFoundPage;