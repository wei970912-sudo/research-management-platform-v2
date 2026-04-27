import { createRouter, createWebHistory } from "vue-router";
import { ensureAuthLoaded } from "../auth";
import DashboardView from "../views/DashboardView.vue";
import PortalView from "../views/PortalView.vue";
import ProjectListView from "../views/ProjectListView.vue";
import ProjectDetailView from "../views/ProjectDetailView.vue";
import ScheduleView from "../views/ScheduleView.vue";
import MeetingView from "../views/MeetingView.vue";
import ContractView from "../views/ContractView.vue";
import DocumentView from "../views/DocumentView.vue";
import AssistantView from "../views/AssistantView.vue";
import LoginView from "../views/LoginView.vue";
import AccountView from "../views/AccountView.vue";
import UserManagementView from "../views/UserManagementView.vue";
import StakeholderView from "../views/StakeholderView.vue";

const routes = [
  { path: "/", redirect: "/portal" },
  { path: "/login", name: "login", component: LoginView, meta: { public: true } },
  { path: "/portal", name: "portal", component: PortalView },
  { path: "/dashboard", name: "dashboard", component: DashboardView },
  { path: "/projects", name: "projects", component: ProjectListView },
  { path: "/projects/:id", name: "project-detail", component: ProjectDetailView, props: true },
  { path: "/schedule", name: "schedule", component: ScheduleView },
  { path: "/stakeholders", name: "stakeholders", component: StakeholderView },
  { path: "/meetings", name: "meetings", component: MeetingView },
  { path: "/contracts", name: "contracts", component: ContractView },
  { path: "/documents", name: "documents", component: DocumentView },
  { path: "/schedule/stakeholders", redirect: "/stakeholders" },
  { path: "/assistant", name: "assistant", component: AssistantView },
  { path: "/account", name: "account", component: AccountView },
  { path: "/users", name: "users", component: UserManagementView, meta: { adminOnly: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  if (to.meta.public) {
    return true;
  }

  const user = await ensureAuthLoaded();
  if (!user) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  if (to.meta.adminOnly && user.role !== "ADMIN") {
    return { path: "/portal" };
  }

  return true;
});

export default router;


