import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || "60s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500"],
    checks: ["rate>0.99"],
  },
};

const apiBase = __ENV.BASE_URL || "http://127.0.0.1:9090/api";

export default function () {
  const res = http.get(`${apiBase}/goods?page=1&size=10`);
  let bodyCode = null;
  try {
    bodyCode = JSON.parse(res.body).code;
  } catch (e) {
    bodyCode = null;
  }

  check(res, {
    "goods list status 200": (r) => r.status === 200,
    "goods list business code 0": () => bodyCode === 0,
  });

  sleep(0.5);
}
