import apiClient from './apiClient';

export const awarenessApi = {
  getTopics: async (category, query) => {
    const params = {};
    if (category) params.category = category;
    if (query) params.q = query;
    const res = await apiClient.get('/awareness/topics', { params });
    return res.data;
  },

  getTopicBySlug: async (slug) => {
    const res = await apiClient.get(`/awareness/topics/${slug}`);
    return res.data;
  },

  getCategories: async () => {
    const res = await apiClient.get('/awareness/categories');
    return res.data;
  }
};
