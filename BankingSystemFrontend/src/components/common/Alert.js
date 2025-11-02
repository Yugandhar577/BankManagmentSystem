// --- src/components/common/Alert.js ---
// (Helper component, you can place this in common/)
import React from 'react';

const Alert = ({ type, message }) => {
  const baseClasses = "p-4 rounded-md my-4";
  const typeClasses = {
    success: "bg-green-100 border border-green-400 text-green-700",
    error: "bg-red-100 border border-red-400 text-red-700",
    info: "bg-blue-100 border border-blue-400 text-blue-700",
  };

  return (
    <div className={`${baseClasses} ${typeClasses[type]}`} role="alert">
      {message}
    </div>
  );
};

export default Alert;