import apiClient from './apiClient';

export const analyticsApi = {
  getOverview: async () => {
    const res = await apiClient.get('/admin/analytics/overview');
    return res.data;
  },

  getCategoryBreakdown: async () => {
    const res = await apiClient.get('/admin/analytics/categories');
    return res.data;
  },

  getChannelBreakdown: async () => {
    const res = await apiClient.get('/admin/analytics/channels');
    return res.data;
  },

  getAssessmentStats: async () => {
    const res = await apiClient.get('/admin/analytics/assessments');
    return res.data;
  },

  getMlDiagnostics: async () => {
    const res = await apiClient.get('/admin/analytics/ml-diagnostics');
    return res.data;
  },

  downloadIncidentsCsv: async () => {
    const response = await apiClient.get('/admin/analytics/export/incidents', {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'cybersaarthi_incidents_deidentified.csv');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  downloadAssessmentsCsv: async () => {
    const response = await apiClient.get('/admin/analytics/export/assessments', {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'cybersaarthi_assessments_deidentified.csv');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }
};
