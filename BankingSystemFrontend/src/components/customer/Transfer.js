// --- src/components/customer/Transfer.js ---
import React, { useState } from 'react';
import api from '../../services/api';
import Alert from '../common/Alert';
import { useNavigate } from 'react-router-dom';

const Transfer = () => {
  const [amount, setAmount] = useState('');
  const [toAccountNumber, setToAccountNumber] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);

    try {
      const response = await api.post('/transaction/transfer', {
        amount: parseFloat(amount),
        toAccountNumber,
        description,
      });
      setSuccess(`Successfully transferred $${parseFloat(amount).toFixed(2)} to account ${toAccountNumber}. New balance: $${response.data.newBalance.toFixed(2)}`);
      setAmount('');
      setToAccountNumber('');
      setDescription('');
      setTimeout(() => navigate('/dashboard'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Transfer failed. Check account number and funds.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-lg mx-auto bg-white p-8 rounded-lg shadow-lg">
      <h2 className="text-3xl font-bold text-center text-gray-800 mb-6">Transfer Funds</h2>
      {error && <Alert type="error" message={error} />}
      {success && <Alert type="success" message={success} />}
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="toAccount">
            Recipient Account Number
          </label>
          <input
            id="toAccount" type="text" value={toAccountNumber} onChange={(e) => setToAccountNumber(e.target.value)}
            required
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="amount">
            Amount
          </label>
          <input
            id="amount" type="number" value={amount} onChange={(e) => setAmount(e.target.value)}
            required min="0.01" step="0.01"
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="description">
            Description (Optional)
          </label>
          <input
            id="description" type="text" value={description} onChange={(e) => setDescription(e.target.value)}
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="flex items-center justify-between">
          <button
            type="submit" disabled={loading}
            className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline w-full disabled:bg-gray-400"
          >
            {loading ? 'Processing...' : 'Transfer'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default Transfer;