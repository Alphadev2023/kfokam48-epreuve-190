import { appelApi } from "./client";
import type { Promotion } from "./types";

export function listerPromotions(): Promise<Promotion[]> {
  return appelApi<Promotion[]>("/api/promotions");
}
