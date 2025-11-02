// --- src/components/common/Footer.js ---
import React from 'react';

const Footer = () => {
  return (
    <footer className="bg-gray-800 text-gray-300 p-4 mt-8">
      <div className="container mx-auto text-center">
        &copy; {new Date().getFullYear()} BankSys. All rights reserved.
      </div>
    </footer>
  );
};

export default Footer;