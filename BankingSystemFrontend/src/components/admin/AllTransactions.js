// --- src/components/admin/AllTransactions.js ---
import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import Alert from '../common/Alert';

// Simple spinner component
const Spinner = () => (
  <div className="border-4 border-gray-200 border-t-blue-500 rounded-full w-12 h-12 animate-spin" role="status">
    <span className="sr-only">Loading...</span>
  </div>
);

const AllTransactions = () => {
  const [transactions, setTransactions] =useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        setLoading(true);
        const response = await api.get('/admin/transactions');
        setTransactions(response.data);
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load transaction history.");
      } finally {
        setLoading(false);
      }
    };
    fetchHistory();
  }, []);

  if (loading) {
    return <div className="flex justify-center"><Spinner /></div>;
  }

  if (error) {
    return <Alert type="error" message={error} />;
  }

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg">
      <h2 className="text-3xl font-semibold text-gray-800 mb-6">All System Transactions</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Account ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {transactions.length > 0 ? (
              transactions.map((tx) => (
                <tr key={tx.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(tx.date).toLocaleString()}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-700">{tx.accountId}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{tx.type}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{tx.description}</td>
                  <td className={`px-6 py-4 whitespace-nowrap text-sm text-right font-medium ${tx.type === 'Deposit' ? 'text-green-600' : 'text-red-600'}`}>
                    {tx.type === 'Deposit' ? '+' : '-'}${tx.amount.toFixed(2)}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={5} className="px-6 py-4 text-center text-gray-500">No transactions found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AllTransactions;