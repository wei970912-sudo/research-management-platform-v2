import http from "./http";

export const login = async (payload) => {
  const { data } = await http.post("/auth/login", payload);
  return data.data;
};

export const logout = async () => {
  const { data } = await http.post("/auth/logout");
  return data.data;
};

export const changePassword = async (payload) => {
  const { data } = await http.post("/auth/change-password", payload);
  return data.data;
};

export const fetchCurrentUser = async () => {
  const { data } = await http.get("/auth/me");
  return data.data;
};

export const fetchUsers = async () => {
  const { data } = await http.get("/auth/users");
  return data.data;
};

export const createUser = async (payload) => {
  const { data } = await http.post("/auth/users", payload);
  return data.data;
};

export const resetUserPassword = async (username, payload) => {
  const { data } = await http.post(`/auth/users/${encodeURIComponent(username)}/reset-password`, payload);
  return data.data;
};

export const fetchDashboardSummary = async () => {
  const { data } = await http.get("/dashboard/summary");
  return data.data;
};

export const fetchFoundationSnapshot = async () => {
  const { data } = await http.get("/foundation/snapshot");
  return data.data;
};

export const fetchProjects = async () => {
  const { data } = await http.get("/projects");
  return data.data;
};

export const createProject = async (payload) => {
  const { data } = await http.post("/projects", payload);
  return data.data;
};

export const updateProject = async (projectId, payload) => {
  const { data } = await http.put(`/projects/${projectId}`, payload);
  return data.data;
};

export const deleteProject = async (projectId) => {
  const { data } = await http.delete(`/projects/${projectId}`);
  return data.data;
};

export const fetchProjectDetail = async (projectId) => {
  const { data } = await http.get(`/projects/${projectId}`);
  return data.data;
};

export const fetchScheduleOverview = async () => {
  const { data } = await http.get("/schedule/overview");
  return data.data;
};

export const fetchPhases = async () => {
  const { data } = await http.get("/schedule/phases");
  return data.data;
};

export const createPhase = async (payload) => {
  const { data } = await http.post("/schedule/phases", payload);
  return data.data;
};

export const updatePhase = async (phaseId, payload) => {
  const { data } = await http.put(`/schedule/phases/${phaseId}`, payload);
  return data.data;
};

export const deletePhase = async (phaseId) => {
  const { data } = await http.delete(`/schedule/phases/${phaseId}`);
  return data.data;
};

export const fetchMilestones = async () => {
  const { data } = await http.get("/schedule/milestones");
  return data.data;
};

export const fetchControlLines = async () => {
  const { data } = await http.get("/schedule/control-lines");
  return data.data;
};

export const createMilestone = async (payload) => {
  const { data } = await http.post("/schedule/milestones", payload);
  return data.data;
};

export const updateMilestone = async (milestoneId, payload) => {
  const { data } = await http.put(`/schedule/milestones/${milestoneId}`, payload);
  return data.data;
};

export const deleteMilestone = async (milestoneId) => {
  const { data } = await http.delete(`/schedule/milestones/${milestoneId}`);
  return data.data;
};

export const createControlLine = async (payload) => {
  const { data } = await http.post("/schedule/control-lines", payload);
  return data.data;
};

export const createControlLineStructure = async (payload) => {
  const { data } = await http.post("/schedule/control-lines/structured", payload, { timeout: 180000 });
  return data.data;
};

export const updateControlLine = async (controlLineId, payload) => {
  const { data } = await http.put(`/schedule/control-lines/${controlLineId}`, payload);
  return data.data;
};

export const updateControlLineStructure = async (controlLineId, payload) => {
  const { data } = await http.put(`/schedule/control-lines/${controlLineId}/structured`, payload, { timeout: 180000 });
  return data.data;
};

export const deleteControlLine = async (controlLineId) => {
  const { data } = await http.delete(`/schedule/control-lines/${controlLineId}`);
  return data.data;
};

export const createControlPoint = async (payload) => {
  const { data } = await http.post("/schedule/control-points", payload);
  return data.data;
};

export const updateControlPoint = async (controlPointId, payload) => {
  const { data } = await http.put(`/schedule/control-points/${controlPointId}`, payload);
  return data.data;
};

export const deleteControlPoint = async (controlPointId) => {
  const { data } = await http.delete(`/schedule/control-points/${controlPointId}`);
  return data.data;
};

export const createControlPointItem = async (payload) => {
  const { data } = await http.post("/schedule/control-point-items", payload);
  return data.data;
};

export const updateControlPointItem = async (itemId, payload) => {
  const { data } = await http.put(`/schedule/control-point-items/${itemId}`, payload);
  return data.data;
};

export const deleteControlPointItem = async (itemId) => {
  const { data } = await http.delete(`/schedule/control-point-items/${itemId}`);
  return data.data;
};

export const fetchMeetings = async () => {
  const { data } = await http.get("/meetings");
  return data.data;
};

export const fetchContracts = async () => {
  const { data } = await http.get("/contracts");
  return data.data;
};

export const fetchTaskBookItems = async () => {
  const { data } = await http.get("/contracts/task-book-items");
  return data.data;
};

export const createContract = async (payload) => {
  const { data } = await http.post("/contracts", payload);
  return data.data;
};

export const createTaskBookItem = async (payload) => {
  const { data } = await http.post("/contracts/task-book-items", payload);
  return data.data;
};

export const updateContract = async (contractId, payload) => {
  const { data } = await http.put(`/contracts/${contractId}`, payload);
  return data.data;
};

export const updateTaskBookItem = async (taskBookItemId, payload) => {
  const { data } = await http.put(`/contracts/task-book-items/${taskBookItemId}`, payload);
  return data.data;
};

export const deleteContract = async (contractId) => {
  const { data } = await http.delete(`/contracts/${contractId}`);
  return data.data;
};

export const deleteTaskBookItem = async (taskBookItemId) => {
  const { data } = await http.delete(`/contracts/task-book-items/${taskBookItemId}`);
  return data.data;
};

export const fetchCalendarEvents = async () => {
  const { data } = await http.get("/calendar/events");
  return data.data;
};

export const createCalendarEvent = async (payload) => {
  const { data } = await http.post("/calendar/events", payload);
  return data.data;
};

export const updateCalendarEvent = async (eventId, payload) => {
  const { data } = await http.put(`/calendar/events/${eventId}`, payload);
  return data.data;
};

export const deleteCalendarEvent = async (eventId) => {
  const { data } = await http.delete(`/calendar/events/${eventId}`);
  return data.data;
};

export const fetchDocuments = async () => {
  const { data } = await http.get("/documents");
  return data.data;
};

export const createDocument = async (payload) => {
  const { data } = await http.post("/documents", payload);
  return data.data;
};

export const updateDocument = async (documentId, payload) => {
  const { data } = await http.put(`/documents/${documentId}`, payload);
  return data.data;
};

export const deleteDocument = async (documentId) => {
  const { data } = await http.delete(`/documents/${documentId}`);
  return data.data;
};

export const uploadDocumentAttachments = async (documentId, files) => {
  const formData = new FormData();
  Array.from(files || []).forEach((file) => formData.append("files", file));
  const { data } = await http.post(`/documents/${documentId}/attachments`, formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
  return data.data;
};

export const buildDocumentAttachmentUrl = (documentId, fileName) =>
  `/api/documents/${encodeURIComponent(documentId)}/attachments/${encodeURIComponent(fileName)}`;

export const createMeeting = async (payload) => {
  const { data } = await http.post("/meetings", payload);
  return data.data;
};

export const updateMeeting = async (meetingId, payload) => {
  const { data } = await http.put(`/meetings/${meetingId}`, payload);
  return data.data;
};

export const deleteMeeting = async (meetingId) => {
  const { data } = await http.delete(`/meetings/${meetingId}`);
  return data.data;
};

export const uploadMeetingAttachments = async (meetingId, files) => {
  const formData = new FormData();
  Array.from(files || []).forEach((file) => formData.append("files", file));
  const { data } = await http.post(`/meetings/${meetingId}/attachments`, formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
  return data.data;
};

export const askAssistant = async (payload) => {
  const { data } = await http.post("/assistant/chat", payload, { timeout: 180000 });
  return data.data;
};

export const fetchStakeholders = async () => {
  const { data } = await http.get("/stakeholders");
  return data.data;
};

export const createStakeholder = async (payload) => {
  const { data } = await http.post("/stakeholders", payload);
  return data.data;
};

export const updateStakeholder = async (stakeholderId, payload) => {
  const { data } = await http.put(`/stakeholders/${stakeholderId}`, payload);
  return data.data;
};

export const deleteStakeholder = async (stakeholderId) => {
  const { data } = await http.delete(`/stakeholders/${stakeholderId}`);
  return data.data;
};
