// --- src/components/customer/Dashboard.js ---
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import Alert from '../common/Alert'; // Using the helper

// Simple spinner component
const Spinner = () => (
  <div className="border-4 border-gray-200 border-t-blue-500 rounded-full w-12 h-12 animate-spin" role="status">
    <span className="sr-only">Loading...</span>
  </div>
);

const Dashboard = () => {
  const { user } = useAuth();
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!user) return;
      try {
        setLoading(true);
        const accResponse = await api.get(`/account/details?userId=${user.id}`);
        setAccount(accResponse.data);

        const txResponse = await api.get(`/transaction/history`);
        setTransactions(txResponse.data.slice(0, 5)); // Show 5 most recent
        
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load dashboard data.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user]);

  if (loading) {
    return <div className="flex justify-center"><Spinner /></div>;
  }

  if (error) {
    return <Alert type="error" message={error} />;
  }

  return (
    <div className="space-y-8">
      <h1 className="text-3xl font-bold text-gray-800">Welcome, {user?.firstName}!</h1>

      <div className="bg-white p-6 rounded-lg shadow-lg">
        <h2 className="text-xl font-semibold text-gray-600 mb-2">Your Balance</h2>
        <p className="text-5xl font-bold text-blue-700">
          ${account ? account.balance.toFixed(2) : '0.00'}
        </p>
        <p className="text-gray-500 mt-2">
          Account Number: {account?.accountNumber} ({account?.accountType})
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Link to="/deposit" className="bg-green-500 hover:bg-green-600 text-white text-center font-bold py-4 px-6 rounded-lg shadow-md transition duration-300">
          Deposit
        </Link>
        <Link to="/withdraw" className="bg-yellow-500 hover:bg-yellow-600 text-white text-center font-bold py-4 px-6 rounded-lg shadow-md transition duration-300">
          Withdraw
        </Link>
        <Link to="/transfer" className="bg-blue-500 hover:bg-blue-600 text-white text-center font-bold py-4 px-6 rounded-lg shadow-md transition duration-300">
          Transfer
        </Link>
      </div>

      <div className="bg-white p-6 rounded-lg shadow-lg">
        <h2 className="text-2xl font-semibold text-gray-800 mb-4">Recent Transactions</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {transactions.length > 0 ? (
                transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(tx.date).toLocaleDateString()}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{tx.type}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{tx.description}</td>
                    <td className={`px-6 py-4 whitespace-nowrap text-sm text-right font-medium ${tx.type === 'Deposit' ? 'text-green-600' : 'text-red-600'}`}>
                      {tx.type === 'Deposit' ? '+' : '-'}${tx.amount.toFixed(2)}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="px-6 py-4 text-center text-gray-500">No recent transactions.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <div className="text-right mt-4">
          <Link to="/history" className="text-blue-600 hover:underline font-medium">
            View All Transactions &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;