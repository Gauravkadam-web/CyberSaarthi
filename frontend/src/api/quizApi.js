import apiClient from './apiClient';

export const quizApi = {
  getQuizForTopic: async (topicId) => {
    const res = await apiClient.get(`/quizzes/topics/${topicId}`);
    return res.data;
  },

  submitAttempt: async (topicId, answers) => {
    const res = await apiClient.post('/quizzes/attempts', { topicId, answers });
    return res.data;
  },

  getMyAttempts: async () => {
    const res = await apiClient.get('/quizzes/my-attempts');
    return res.data;
  }
};
