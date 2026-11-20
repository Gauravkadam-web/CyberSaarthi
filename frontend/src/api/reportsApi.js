import apiClient from './apiClient';

export const reportsApi = {
  submitReport: async (reportData) => {
    const res = await apiClient.post('/reports', reportData);
    return res.data;
  },

  getMyReports: async () => {
    const res = await apiClient.get('/reports');
    return res.data;
  },

  getReportById: async (id) => {
    const res = await apiClient.get(`/reports/${id}`);
    return res.data;
  },

  trackReportByReferenceCode: async (referenceCode) => {
    const res = await apiClient.get(`/reports/track/${encodeURIComponent(referenceCode)}`);
    return res.data;
  },

  // Administrator Endpoints
  getAdminQueue: async ({ status, query, page = 0, size = 10 } = {}) => {
    const params = { page, size };
    if (status) params.status = status;
    if (query) params.query = query;
    const res = await apiClient.get('/admin/reports', { params });
    return res.data;
  },

  getAdminReportById: async (id) => {
    const res = await apiClient.get(`/admin/reports/${id}`);
    return res.data;
  },

  updateStatus: async (id, { newStatus, reason, reviewedCategoryId }) => {
    const res = await apiClient.patch(`/admin/reports/${id}/status`, {
      newStatus,
      reason,
      reviewedCategoryId: reviewedCategoryId || null
    });
    return res.data;
  },

  addReviewNote: async (id, { noteText, visibility }) => {
    const res = await apiClient.post(`/admin/reports/${id}/notes`, {
      noteText,
      visibility
    });
    return res.data;
  }
};
