import { request } from "./client.js";

export const getCommercialReport = (staff) => request(`/${staff}/reports/commercial`);
export const getMyPerformance = () => request("/seller/reports/performance");
export const getSellerPerformance = (staff, sellerId) =>
  request(`/${staff}/sellers/${encodeURIComponent(sellerId)}/performance-report`);
